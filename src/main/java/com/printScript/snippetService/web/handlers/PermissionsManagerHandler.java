package com.printScript.snippetService.web.handlers;

import static com.printScript.snippetService.web.RequestExecutor.getRequest;
import static com.printScript.snippetService.web.RequestExecutor.postRequest;

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
import com.printScript.snippetService.services.RestTemplateService;

@Component
public class PermissionsManagerHandler {

    private final RestTemplate permissionsWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PermissionsManagerHandler(RestTemplateService permissionsRestTemplate, ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
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

    public Response<String> checkPermissions(String snippetId, String token, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestPermissions = new HttpEntity<>(headers);
        Map<String, String> params = Map.of("snippetId", snippetId);
        try {
            getRequest(permissionsWebClient, path, requestPermissions, Void.class, params);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<String> shareSnippet(String token, ShareSnippetDTO shareSnippetDTO, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ShareSnippetDTO> requestPermissions = new HttpEntity<>(shareSnippetDTO, headers);
        try {
            postRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<List<String>> getSnippetRelationships(String token, String filterType) {
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", token);
        HttpEntity<Void> requestPermissions = new HttpEntity<>(header);
        try {
            String response = getRequest(permissionsWebClient, "/get/relationships", requestPermissions, String.class,
                    Map.of("filterType", filterType));
            List<String> snippetIds = objectMapper.readValue(response, new TypeReference<>() {
            });
            return Response.withData(snippetIds);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        } catch (JsonProcessingException e) {
            return Response.withError(new Error<>(400, "Error parsing response"));
        }
    }
}
