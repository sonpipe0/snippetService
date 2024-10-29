package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.errorDTO.ErrorMessage;
import com.printScript.snippetService.redis.LintProducerInterface;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.BucketRequestExecutor;
import com.printScript.snippetService.web.SnippetServiceWebHandler;

import jakarta.transaction.Transactional;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private BucketRequestExecutor bucketRequestExecutor;

    @Autowired
    private SnippetServiceWebHandler webHandler;

    private final LintProducerInterface lintProducer;

    @Autowired
    public SnippetService(LintProducerInterface lintProducer) {
        this.lintProducer = lintProducer;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        String title = snippetDTO.getTitle();
        String language = snippetDTO.getLanguage();
        String version = snippetDTO.getVersion();
        String code = snippetDTO.getCode();

        Response<String> validation = validateRequest(
                Map.of("title", title, "language", language, "version", version, "code", code));
        if (validation.isError())
            return validation;

        Snippet snippet = new Snippet();
        snippet.setTitle(title);
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setVersion(version);

        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        String snippetId = snippet.getId();

        Response<String> permissionsResponse = webHandler.saveRelation(token, snippetId, "/snippets/save/relationship");
        if (permissionsResponse.isError()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return permissionsResponse;
        }

        Response<String> printScriptResponse = webHandler.validateCode(code, version, token);
        if (printScriptResponse.isError()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return printScriptResponse;
        }

        Response<Void> response = bucketRequestExecutor.put("snippets/" + snippetId, code, token);
        if (response.isError())
            return Response.withError(response.getError());

        return Response.withData(snippetId);
    }

    public Response<String> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        String snippetId = updateSnippetDTO.getSnippetId();
        String title = updateSnippetDTO.getTitle();
        String language = updateSnippetDTO.getLanguage();
        String version = updateSnippetDTO.getVersion();
        String code = updateSnippetDTO.getCode();

        Response<String> validation = validateRequest(
                Map.of("snippetId", snippetId, "title", title, "language", language, "version", version, "code", code));
        if (validation.isError())
            return validation;

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = webHandler.checkPermissions(snippetId, token, "/snippets/can-edit");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> printScriptResponse = webHandler.validateCode(code, version, token);
        if (printScriptResponse.isError())
            return printScriptResponse;
        Snippet snippet = snippetOptional.get();
        snippet.setTitle(title);
        snippet.setDescription(updateSnippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setVersion(version);

        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        bucketRequestExecutor.put("snippets/" + snippetId, code, token);
        return Response.withData("Snippet updated successfully");
    }

    public Response<SnippetDetails> getSnippetDetails(String snippetId, String userId, String token,
            MultipartFile configFile) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = webHandler.checkPermissions(snippetId, token, "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetOpt.get();

        Response<String> response = bucketRequestExecutor.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        String code = response.getData();
        String version = snippet.getVersion();
        InputStream config;
        try {
            config = configFile.getInputStream();
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        Response<List<ErrorMessage>> printScriptResponse = webHandler.getLintingErrors(code, version, token, config);
        if (printScriptResponse.isError())
            return Response.withError(printScriptResponse.getError());

        SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(),
                snippet.getLanguage(), version, code, printScriptResponse.getData());

        return Response.withData(snippetDetails);
    }

    public Response<String> shareSnippet(String userId, ShareSnippetDTO shareSnippetDTO, String token) {
        String snippetId = shareSnippetDTO.getSnippetId();

        Response<String> permissionsResponse = webHandler.checkPermissions(snippetId, token, "/snippets/has-access");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> permissionsResponse2 = webHandler.shareSnippet(token, shareSnippetDTO.getUsername(), snippetId,
                "/snippets/save/share/relationship");
        if (permissionsResponse2.isError()) {
            return permissionsResponse2;
        }

        return Response.withData("Snippet shared successfully");
    }

    public void postToCyclon() {
        lintProducer.publishEvent("ciclon");
    }
}
