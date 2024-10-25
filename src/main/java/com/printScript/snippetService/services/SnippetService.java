package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.errorDTO.ErrorMessage;
import com.printScript.snippetService.redis.LintProducerInterface;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.BucketRequestExecutor;

import jakarta.transaction.Transactional;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private BucketRequestExecutor bucketRequestExecutor;

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;
    private final LintProducerInterface lintProducer;

    @Autowired
    public SnippetService(RestTemplateService permissionsRestTemplate, RestTemplateService printScriptRestTemplate,
            ObjectMapper objectMapper, LintProducerInterface lintProducer) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
        this.lintProducer = lintProducer;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        String userId = snippetDTO.getUserId();
        String title = snippetDTO.getTitle();
        String language = snippetDTO.getLanguage();
        String version = snippetDTO.getVersion();
        String code = snippetDTO.getCode();

        Response<String> validation = validateRequest(
                Map.of("userId", userId, "title", title, "language", language, "version", version, "code", code));
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

        Response<String> permissionsResponse = saveRelation(token, userId, snippetId, "/snippets/save/relationship");
        if (permissionsResponse.isError()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return permissionsResponse;
        }

        Response<String> printScriptResponse = validateCode(code, version);
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
        String userId = updateSnippetDTO.getUserId();
        String snippetId = updateSnippetDTO.getSnippetId();
        String title = updateSnippetDTO.getTitle();
        String language = updateSnippetDTO.getLanguage();
        String version = updateSnippetDTO.getVersion();
        String code = updateSnippetDTO.getCode();

        Response<String> validation = validateRequest(Map.of("snippetId", snippetId, "userId", userId, "title", title,
                "language", language, "version", version, "code", code));
        if (validation.isError())
            return validation;

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token, "/snippets/canEdit");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> printScriptResponse = validateCode(code, version);
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

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token, "/snippets/hasAccess");
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

        Response<List<ErrorMessage>> printScriptResponse = getLintingErrors(code, version, config);
        if (printScriptResponse.isError())
            return Response.withError(printScriptResponse.getError());

        SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(),
                snippet.getLanguage(), version, code, printScriptResponse.getData());

        return Response.withData(snippetDetails);
    }

    public Response<String> shareSnippet(String userId, ShareSnippetDTO shareSnippetDTO, String token) {
        String snippetId = shareSnippetDTO.getSnippetId();

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token, "/snippets/hasAccess");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> permissionsResponse2 = saveRelation(token, shareSnippetDTO.getToUserId(), snippetId,
                "/snippets/save/share/relationship");
        if (permissionsResponse2.isError()) {
            return permissionsResponse2;
        }

        return Response.withData("Snippet shared successfully");
    }

    private Response<String> saveRelation(String token, String userId, String snippetId, String path) {
        HttpEntity<Permissions> requestPermissions = createPostPermissionsRequest(userId, snippetId, token);
        try {
            postRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    private Response<String> checkPermissions(String snippetId, String userId, String token, String path) {
        HttpEntity<Void> requestPermissions = createGetPermissionsRequest(token);
        Map<String, String> params = Map.of("snippetId", snippetId, "userId", userId);
        try {
            getRequest(permissionsWebClient, path, requestPermissions, Void.class, params);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    private Response<String> validateCode(String code, String version) {
        HttpEntity<Validation> requestPrintScript = createValidatePrintScriptRequest(code, version);
        try {
            postRequest(printScriptWebClient, "/runner/validate", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return getValidationErrors(e);
        }
    }

    private Response<String> getValidationErrors(HttpClientErrorException e) {
        try {
            List<ErrorMessage> errorMessages = objectMapper.readValue(e.getResponseBodyAsString(),
                    new TypeReference<>() {
                    });
            return Response.withError(new Error<>(e.getStatusCode().value(), errorMessages));
        } catch (JsonProcessingException ex) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public void postToCyclon() {
        lintProducer.publishEvent("ciclon");
    }

    private Response<List<ErrorMessage>> getLintingErrors(String code, String version, InputStream config) {
        HttpEntity<Linting> requestPrintScript = createLintPrintScriptRequest(code, version, config);
        try {
            String lintingErrors = getRequest(printScriptWebClient, "/runner/lintingErrors", requestPrintScript,
                    String.class, Map.of());
            List<ErrorMessage> lintingErrorsList = objectMapper.readValue(lintingErrors, new TypeReference<>() {
            });
            if (lintingErrorsList.isEmpty()) {
                return Response.withData(null);
            } else {
                return Response.withData(lintingErrorsList);
            }
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        } catch (JsonProcessingException e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }
}
