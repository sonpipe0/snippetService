package com.printScript.snippetService.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestExecutor {

    public static <T> T getRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType,
            Map<String, String> params) {
        Map<String, List<String>> multiValueParams = params.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> Stream.of(e.getValue()).collect(Collectors.toList())));

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(createUrl(webClient, path))
                .queryParams(CollectionUtils.toMultiValueMap(multiValueParams)).toUriString();

        ResponseEntity<T> response = webClient.exchange(urlTemplate, HttpMethod.GET, request, responseType, params);
        return response.getBody();
    }

    public static <T> T postRequest(RestTemplate webClient, String path, HttpEntity<?> request, Class<T> responseType) {
        String url = createUrl(webClient, path);
        return webClient.postForEntity(url, request, responseType).getBody();
    }

    public static <T> T deleteRequest(RestTemplate webClient, String path, HttpEntity<?> request,
            Class<T> responseType) {
        String url = createUrl(webClient, path);
        return webClient.exchange(url, HttpMethod.DELETE, request, responseType).getBody();
    }

    public static void putRequest(RestTemplate webClient, String path, HttpEntity<?> request) {
        String url = createUrl(webClient, path);
        webClient.put(url, request);
    }

    private static String createUrl(RestTemplate printScriptWebClient, String path) {
        String rootUri = printScriptWebClient.getUriTemplateHandler().expand("/").toString();
        return UriComponentsBuilder.fromHttpUrl(rootUri).path(path).toUriString();
    }
}
