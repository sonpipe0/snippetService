package com.printScript.snippetService.utils;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.printScript.snippetService.DTO.Permissions;
import com.printScript.snippetService.DTO.Validation;

public class Utils {
    public static ResponseEntity<Object> checkMediaType(String contentType) {
        if (contentType == null || (!contentType.equals("text/plain") && !contentType.equals("application/json"))) {
            return new ResponseEntity<>("Unsupported file type", HttpStatusCode.valueOf(415));
            // 415 Unsupported Media Type
        }
        return null;
    }

    public static HttpEntity<Permissions> createPostPermissionsRequest(String userId, String snippetId, String token) {
        Permissions permissions = new Permissions(userId, snippetId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return new HttpEntity<>(permissions, headers);
    }

    public static HttpEntity<Void> createGetPermissionsRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return new HttpEntity<>(headers);
    }

    public static HttpEntity<Validation> createPrintScriptRequest(String code, String version) {
        Validation validation = new Validation(code, version);
        return new HttpEntity<>(validation);
    }

    public static HttpEntity<MultiValueMap<String, Object>> createPrintScriptFileRequest(MultipartFile file,
            String version) throws IOException {
        MultiValueMap<String, Object> body = toMultiValueMap(file, version);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(body, headers);
    }

    public static String createUrl(RestTemplate printScriptWebClient, String path) {
        String rootUri = printScriptWebClient.getUriTemplateHandler().expand("/").toString();
        return UriComponentsBuilder.fromHttpUrl(rootUri).path(path).toUriString();
    }

    private static MultiValueMap<String, Object> toMultiValueMap(MultipartFile file, String version)
            throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        map.add("version", version);

        return map;
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
