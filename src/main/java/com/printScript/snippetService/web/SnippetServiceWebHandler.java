package com.printScript.snippetService.web;

import static com.printScript.snippetService.utils.Utils.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public Response<String> validateCode(String code, String version, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Validation> requestPrintScript = new HttpEntity<>(new Validation(code, version), headers);
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

    public Response<Void> getLintingErrors(String code, String version, String language, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Lint> requestPrintScript = new HttpEntity<>(new Lint(code, version), headers);
        try {
            postRequest(printScriptWebClient, "/runner/lintingErrors", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            String errors = e.getResponseBodyAsString();
            try {
                List<ErrorMessage> errorMessages = objectMapper.readValue(errors, new TypeReference<>() {
                });
                return Response.withError(new Error<>(e.getStatusCode().value(), errorMessages));
            } catch (JsonProcessingException ex) {
                return Response.withError(new Error<>(500, errors));
            }
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

    private <T> T getRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType,
            Map<String, String> params) {
        Map<String, List<String>> multiValueParams = params.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> Stream.of(e.getValue()).collect(Collectors.toList())));

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(createUrl(webClient, path))
                .queryParams(CollectionUtils.toMultiValueMap(multiValueParams)).toUriString();

        ResponseEntity<T> response = webClient.exchange(urlTemplate, HttpMethod.GET, request, responseType, params);
        return response.getBody();
    }

    private <T> T postRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType) {
        String url = createUrl(webClient, path);
        return webClient.postForEntity(url, request, responseType).getBody();
    }
}
