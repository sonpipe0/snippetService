package com.printScript.snippetService.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.DTO.Validation;
import com.printScript.snippetService.errorDTO.Error;

import jakarta.validation.ConstraintViolation;

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
        Map<String, List<String>> multiValueParams = params.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> Stream.of(e.getValue()).collect(Collectors.toList())));

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(createUrl(webClient, path))
                .queryParams(CollectionUtils.toMultiValueMap(multiValueParams)).toUriString();

        ResponseEntity<T> response = webClient.exchange(urlTemplate, HttpMethod.GET, request, responseType, params);
        return response.getBody();
    }

    public static void putRequest(RestTemplate webClient, String path, HttpEntity<?> request) {
        String url = createUrl(webClient, path);
        webClient.put(url, request);
    }

    public static <T> Error<?> getViolationsMessageError(Set<ConstraintViolation<T>> violations) {
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("\n");
        }
        return new Error<>(400, message.toString());
    }
}
