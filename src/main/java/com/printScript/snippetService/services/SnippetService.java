package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.MultiValueMap;
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
    private final RestTemplate bucketWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SnippetService(RestTemplateService permissionsRestTemplate, RestTemplateService printScriptRestTemplate,
            RestTemplateService bucketRestTemplate, ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.bucketWebClient = bucketRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        String userId = snippetDTO.getUserId();
        String code = snippetDTO.getCode();
        String version = snippetDTO.getVersion();

        Snippet snippet = new Snippet();
        snippet.setTitle(snippetDTO.getTitle());
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(snippetDTO.getLanguage());
        snippet.setVersion(version);

        List<String> invalidFields = snippet.getInvalidFields();
        if (!invalidFields.isEmpty()) {
            String message = "Invalid body: " + String.join(", ", invalidFields)
                    + (invalidFields.size() > 1 ? " are required" : " is required");
            return Response.withError(new Error<>(400, message));
        }
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


        Response<Void> response = bucketRequestExecutor.put("snippets/" + snippetId, code, token);
        if (response.isError())
            return Response.withError(response.getError());

        return Response.withData(snippetId);
    }

    @Transactional
    public Response<String> saveSnippetFile(MultipartFile file, SnippetInfoDTO snippetInfoDTO, String token) {
        try {
            String userId = snippetInfoDTO.getUserId();
            String version = snippetInfoDTO.getVersion();

            Snippet snippet = new Snippet();
            snippet.setTitle(snippetInfoDTO.getTitle());
            snippet.setDescription(snippetInfoDTO.getDescription());
            snippet.setLanguage(snippetInfoDTO.getLanguage());
            snippetRepository.save(snippet);

            String snippetId = snippet.getId();

            Response<String> permissionsResponse = saveRelation(token, userId, snippetId,
                    "/snippets/save/relationship");
            if (permissionsResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return permissionsResponse;
            }

            Response<String> printScriptResponse = validateFileCode(file, version);
            if (printScriptResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return printScriptResponse;
            }

            Response<Void> response = bucketRequestExecutor.put("snippets/" + snippetId, new String(file.getBytes()),
                    token);
            if (response.isError())
                return Response.withError(response.getError());

            return Response.withData(snippetId);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }

    public Response<String> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        String snippetId = updateSnippetDTO.getSnippetId();
        String userId = updateSnippetDTO.getUserId();
        String code = updateSnippetDTO.getCode();
        String version = updateSnippetDTO.getVersion();

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
        snippet.setTitle(updateSnippetDTO.getTitle());
        snippet.setDescription(updateSnippetDTO.getDescription());
        snippet.setLanguage(updateSnippetDTO.getLanguage());
        snippet.setVersion(version);
        snippetRepository.save(snippet);

        Response<Void> response = bucketRequestExecutor.put("snippets/" + snippetId, code, token);
        return Response.withData("Snippet updated successfully");
    }

    public Response<String> updateSnippetFile(MultipartFile file, UpdateSnippetInfoDTO updateSnippetInfoDTO,
            String token) {
        try {
            String snippetId = updateSnippetInfoDTO.getSnippetId();
            String version = updateSnippetInfoDTO.getVersion();
            String userId = updateSnippetInfoDTO.getUserId();

            Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
            if (snippetOptional.isEmpty()) {
                return Response.withError(new Error<>(404, "Snippet not found"));
            }

            Response<String> permissionsResponse = checkPermissions(snippetId, userId, token, "/snippets/canEdit");
            if (permissionsResponse.isError())
                return permissionsResponse;

            Response<String> printScriptResponse = validateFileCode(file, version);
            if (printScriptResponse.isError())
                return printScriptResponse;

            Snippet snippet = snippetOptional.get();
            snippet.setTitle(updateSnippetInfoDTO.getTitle());
            snippet.setDescription(updateSnippetInfoDTO.getDescription());
            snippet.setLanguage(updateSnippetInfoDTO.getLanguage());
            snippet.setVersion(version);
            snippetRepository.save(snippet);

            Response<Void> response = bucketRequestExecutor.put("snippets/" + snippetId, new String(file.getBytes()),
                    token);
            if (response.isError())
                return Response.withError(response.getError());

            return Response.withData("Snippet updated successfully");
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }

    public Response<SnippetDetails> getSnippetDetails(String snippetId, String userId, String token) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token, "/snippets/hasAccess");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetOpt.get();

        Response<String> code = bucketRequestExecutor.get("snippets/" + snippetId, token);
        if (code.isError())
            return Response.withError(code.getError());

        SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(),
                snippet.getLanguage(), snippet.getVersion(), code.getData());

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
        HttpEntity<Validation> requestPrintScript = createPrintScriptRequest(code, version);
        try {
            postRequest(printScriptWebClient, "/runner/validate", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return getValidationErrors(e);
        }
    }

    private Response<String> validateFileCode(MultipartFile file, String version) {
        try {
            HttpEntity<MultiValueMap<String, Object>> requestPrintScript = createPrintScriptFileRequest(file, version);
            try {
                postRequest(printScriptWebClient, "/runner/validate/file", requestPrintScript, Void.class);
                return Response.withData(null);
            } catch (HttpClientErrorException e) {
                return getValidationErrors(e);
            }
        } catch (Exception err) {
            return Response.withError(new Error<>(500, err.getMessage()));
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
