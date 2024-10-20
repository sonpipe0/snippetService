package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

import jakarta.transaction.Transactional;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SnippetService(RestTemplateService permissionsRestTemplate, RestTemplateService printScriptRestTemplate,
            ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        String userId = snippetDTO.getUserId();
        String code = snippetDTO.getCode();
        String version = snippetDTO.getVersion();

        Snippet snippet = new Snippet();
        snippet.setSnippet(code.getBytes());
        snippet.setTitle(snippetDTO.getTitle());
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(snippetDTO.getLanguage());
        snippet.setVersion(version);

        if (!snippet.isValid()) {
            List<String> invalidFields = snippet.getInvalidFields();
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

        HttpEntity<PermissionsDTO> requestPermissions = createPostPermissionsRequest(userId, snippetId, token);
        try {
            postRequest(permissionsWebClient, "/snippets/save/relationship", requestPermissions, Void.class);
        } catch (HttpClientErrorException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }

        HttpEntity<ValidationDTO> requestPrintScript = createPrintScriptRequest(code, version);
        try {
            postRequest(printScriptWebClient, "/runner/validate", requestPrintScript, Void.class);
        } catch (HttpClientErrorException e) {
            return getValidationErrors(e);
        }

        return Response.withData(snippetId);
    }

    @Transactional
    public Response<String> saveFromMultiPart(MultipartFile file, SnippetInfoDTO snippetInfoDTO, String token) {
        try {
            String userId = snippetInfoDTO.getUserId();
            String version = snippetInfoDTO.getVersion();

            Snippet snippet = new Snippet();
            snippet.setSnippet(file.getBytes());
            snippet.setTitle(snippetInfoDTO.getTitle());
            snippet.setDescription(snippetInfoDTO.getDescription());
            snippet.setLanguage(snippetInfoDTO.getLanguage());
            snippetRepository.save(snippet);

            String snippetId = snippet.getId();

            HttpEntity<PermissionsDTO> requestPermissions = createPostPermissionsRequest(userId, snippetId, token);
            try {
                postRequest(permissionsWebClient, "/snippets/save/relationship", requestPermissions, Void.class);
            } catch (HttpClientErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
            }

            HttpEntity<MultiValueMap<String, Object>> requestPrintScript = createPrintScriptFileRequest(file, version);
            try {
                postRequest(printScriptWebClient, "/runner/validate/file", requestPrintScript, Void.class);
            } catch (HttpClientErrorException e) {
                return getValidationErrors(e);
            }

            return Response.withData(snippetId);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }

    public Response<String> updateSnippet(MultipartFile file, UpdateSnippetDTO updateSnippetDTO, String token) {
        try {
            String snippetId = updateSnippetDTO.getSnippetId();
            String version = updateSnippetDTO.getVersion();
            String userId = updateSnippetDTO.getUserId();

            Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
            if (snippetOptional.isEmpty()) {
                return Response.withError(new Error<>(404, "Snippet not found"));
            }

            Response<Void> permissionsResponse = checkPermissions(snippetId, userId, token);
            if (permissionsResponse.isError()) {
                return Response.withError(permissionsResponse.getError());
            }

            HttpEntity<MultiValueMap<String, Object>> requestPrintScript = createPrintScriptFileRequest(file, version);
            try {
                postRequest(printScriptWebClient, "/runner/validate/file", requestPrintScript, Void.class);
            } catch (HttpClientErrorException e) {
                return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
            }

            Snippet snippet = snippetOptional.get();
            snippet.setSnippet(file.getBytes());
            snippet.setTitle(updateSnippetDTO.getTitle());
            snippet.setDescription(updateSnippetDTO.getDescription());
            snippet.setLanguage(updateSnippetDTO.getLanguage());
            snippet.setVersion(version);
            snippetRepository.save(snippet);

            return Response.withData("Snippet updated successfully");
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal server error"));
        }
    }

    public Response<SnippetDetails> getSnippetDetails(String snippetId, String userId, String token) {
        try {
            Response<Void> permissionsResponse = checkPermissions(snippetId, userId, token);
            if (permissionsResponse.isError()) {
                return Response.withError(permissionsResponse.getError());
            }

            Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
            if (snippetOpt.isEmpty()) {
                return Response.withError(new Error<>(404, "Snippet not found"));
            }

            Snippet snippet = snippetOpt.get();

            SnippetDetails snippetDetails = new SnippetDetails();
            snippetDetails.setId(snippet.getId());
            snippetDetails.setContent(new String(snippet.getSnippet(), StandardCharsets.UTF_8));

            return Response.withData(snippetDetails);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal server error"));
        }
    }

    private Response<Void> checkPermissions(String snippetId, String userId, String token) {
        HttpEntity<Void> requestPermissions = createGetPermissionsRequest(token);
        try {
            String path = "/snippets/hasAccess?snippetId=" + snippetId + "&userId=" + userId;
            getRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    private Response<String> getValidationErrors(HttpClientErrorException e) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        try {
            List<ErrorMessage> errorMessages = objectMapper.readValue(e.getResponseBodyAsString(),
                    new TypeReference<List<ErrorMessage>>() {
                    });
            return Response.withError(new Error<>(e.getStatusCode().value(), errorMessages));
        } catch (JsonProcessingException ex) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }
}
