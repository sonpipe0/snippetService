package com.printScript.snippetService.web.handlers;

import static com.printScript.snippetService.web.RequestExecutor.getRequest;
import static com.printScript.snippetService.web.RequestExecutor.postRequest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.services.RestTemplateService;

@Component
public class PermissionsManagerHandler {

    private final RestTemplate permissionsWebClient;

    @Autowired
    public PermissionsManagerHandler(RestTemplateService permissionsRestTemplate) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
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
}
