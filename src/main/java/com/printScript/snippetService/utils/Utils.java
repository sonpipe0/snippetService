package com.printScript.snippetService.utils;

import java.io.InputStream;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.printScript.snippetService.DTO.Linting;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.DTO.Validation;
import com.printScript.snippetService.errorDTO.Error;

public class Utils {
    public static Response<String> validateRequest(Map<String, String> request) {
        for (Map.Entry<String, String> entry : request.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.trim().isEmpty()) {
                return Response.withError(new Error<>(400, key + " is required"));
            }
        }
        return Response.withData(null);
    }

    public static ResponseEntity<Object> checkMediaType(String contentType) {
        if (contentType == null || (!contentType.equals("text/plain") && !contentType.equals("application/json"))) {
            return new ResponseEntity<>("Unsupported file type", HttpStatusCode.valueOf(415));
            // 415 Unsupported Media Type
        }
        return null;
    }

    public static HttpEntity<Void> createGetPermissionsRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return new HttpEntity<>(headers);
    }

    public static HttpEntity<Validation> createValidatePrintScriptRequest(String code, String version, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        Validation validation = new Validation(code, version);
        return new HttpEntity<>(validation, headers);
    }

    public static HttpEntity<Linting> createLintPrintScriptRequest(String code, String version, InputStream config,
            String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        Linting linting = new Linting(code, version, config);
        return new HttpEntity<>(linting, headers);
    }

    public static String createUrl(RestTemplate printScriptWebClient, String path) {
        String rootUri = printScriptWebClient.getUriTemplateHandler().expand("/").toString();
        return UriComponentsBuilder.fromHttpUrl(rootUri).path(path).toUriString();
    }

    public static <T> T postRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType) {
        String url = createUrl(webClient, path);
        return webClient.postForEntity(url, request, responseType).getBody();
    }

    public static <T> T getRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType,
            Map<String, String> params) {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(createUrl(webClient, path))
                .queryParam("snippetId", "{snippetId}").queryParam("userId", "{userId}").encode().toUriString();

        ResponseEntity<T> response = webClient.exchange(urlTemplate, HttpMethod.GET, request, responseType, params);
        return response.getBody();
    }

    public static void putRequest(RestTemplate webClient, String path, HttpEntity<?> request) {
        String url = createUrl(webClient, path);
        webClient.put(url, request);
    }
}
