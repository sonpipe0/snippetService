package com.printScript.snippetService.web;

import static com.printScript.snippetService.utils.Utils.*;
import static com.printScript.snippetService.utils.Utils.postRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.errorDTO.ErrorMessage;
import com.printScript.snippetService.services.RestTemplateService;

@Component
public class SnippetServiceWebHandler {

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SnippetServiceWebHandler(RestTemplateService permissionsRestTemplate,
            RestTemplateService printScriptRestTemplate, ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    public Response<List<ErrorMessage>> getLintingErrors(String code, String version, String token,
            InputStream config) {
        HttpEntity<Linting> requestPrintScript = createLintPrintScriptRequest(code, version, config, token);
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

    public Response<String> validateCode(String code, String version, String token) {
        HttpEntity<Validation> requestPrintScript = createValidatePrintScriptRequest(code, version, token);
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

    public Response<String> saveRelation(String token, String snippetId, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> requestPermissions = new HttpEntity<>(snippetId, headers);
        try {
            postRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<String> shareSnippet(String token, String userName, String snippetId, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ShareSnippetDTO> requestPermissions = new HttpEntity<>(new ShareSnippetDTO(snippetId, userName),
                headers);
        try {
            postRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<String> checkPermissions(String snippetId, String token, String path) {
        HttpEntity<Void> requestPermissions = createGetPermissionsRequest(token);
        Map<String, String> params = Map.of("snippetId", snippetId);
        try {
            getRequest(permissionsWebClient, path, requestPermissions, Void.class, params);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }
}
