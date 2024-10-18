package com.printScript.snippetService.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.services.WebClientService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

public class Utils {
    public static ResponseEntity<Object> checkMediaType(String contentType) {
        if (contentType == null || (!contentType.equals("text/plain") && !contentType.equals("application/json"))) {
            return new ResponseEntity<>("Unsupported file type", HttpStatusCode.valueOf(415)); // 415 Unsupported Media Type
        }
        return null;
    }

    public static MultiValueMap<String, Object> toMultiValueMap(MultipartFile file, String version) throws IOException {
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

    public static JsonNode executePermissionsGet(WebClientService webClientService, String url, String token, ObjectMapper jacksonObjectMapper) {
        return webClientService.get(url, httpHeaders -> {
            httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", token);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }

    public static JsonNode executePermissionsPost(WebClientService webClientService, String url, String token, Map<String, Object> body, ObjectMapper jacksonObjectMapper) {
        return webClientService.postObject(url, body, httpHeaders -> {
            httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", token);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }

    public static JsonNode executePermissionsPostFile(WebClientService webClientService, String url, String token, MultiValueMap<String, Object> body, ObjectMapper jacksonObjectMapper) {
        return webClientService.uploadMultipart(url, body, httpHeaders -> {
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            httpHeaders.set("Authorization", token);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }

    public static JsonNode executePrintScriptGet(WebClientService webClientService, String url, ObjectMapper jacksonObjectMapper) {
        return webClientService.get(url, httpHeaders -> {
            httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }

    public static JsonNode executePrintScriptPost(WebClientService webClientService, String url, Map<String, Object> body, ObjectMapper jacksonObjectMapper) {
        return webClientService.postObject(url, body, httpHeaders -> {
            httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }

    public static JsonNode executePrintScriptPostFile(WebClientService webClientService, String url, MultiValueMap<String, Object> body, ObjectMapper jacksonObjectMapper) {
        return webClientService.uploadMultipart(url, body, httpHeaders -> {
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        }, error -> {
            Response<Error> errorResponse = Response.errorFromWebFluxError(error);
            return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
        }).block();
    }
}
