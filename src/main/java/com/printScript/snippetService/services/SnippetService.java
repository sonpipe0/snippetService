package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

    public Response<SnippetDetails> getSnippetDetails(String snippetId, String token) {
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
        String language = snippet.getLanguage();

        Response<Void> printScriptResponse = webHandler.getLintingErrors(code, version, language, token);
        List<ErrorMessage> errors = null;

        if (printScriptResponse.getError() != null
                && printScriptResponse.getError().body() instanceof List<?> errorBody) {
            if (!errorBody.isEmpty() && errorBody.getFirst() instanceof ErrorMessage) {
                errors = (List<ErrorMessage>) errorBody;
            }
        }

        SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(),
                snippet.getLanguage(), version, code, errors);
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

    private Response<Map<String, String>> getSnippetRelationships(String userId, String token) {
        HttpEntity<Void> requestPermissions = createGetPermissionsRequest(token);
        Map<String, String> params = Map.of("userId", userId);
        try {
            getRequest(permissionsWebClient, "/snippets/get/relationships", requestPermissions, Void.class, params);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<List<SnippetDetails>> getAccessibleSnippets(String userId, String token, String relation, String nameFilter, String languageFilter, Boolean isValid) {
        Response<Map<String, String>> relationshipsResponse = getSnippetRelationships(userId, token);
        if (relationshipsResponse.isError()) {
            return Response.withError(relationshipsResponse.getError());
        }

        Map<String, String> relationships = relationshipsResponse.getData();
        List<SnippetDetails> accessibleSnippets = new ArrayList<>();

        for (Map.Entry<String, String> entry : relationships.entrySet()) {
            String snippetId = entry.getKey();
            String snippetRelation = entry.getValue();

            if (relation != null && !relation.equals(snippetRelation)) {
                continue;
            }

            Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
            if (snippetOpt.isEmpty()) {
                continue;
            }

            Snippet snippet = snippetOpt.get();
            if (nameFilter != null && !snippet.getTitle().contains(nameFilter)) {
                continue;
            }

            if (languageFilter != null && !snippet.getLanguage().equals(languageFilter)) {
                continue;
            }

            Response<String> codeResponse = bucketRequestExecutor.get("snippets/" + snippetId, token);
            if (codeResponse.isError()) {
                continue;
            }

            SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(), snippet.getLanguage(), snippet.getVersion(), codeResponse.getData());
            accessibleSnippets.add(snippetDetails);
        }

        return Response.withData(accessibleSnippets);
    }

}
