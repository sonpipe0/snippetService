package com.printScript.snippetService.web;

import static com.printScript.snippetService.utils.Utils.createUrl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.services.RestTemplateService;

@Component
public class BucketRequestExecutor {
    private final RestTemplate bucketWebClient;

    public BucketRequestExecutor(RestTemplateService bucketRestTemplate) {
        this.bucketWebClient = bucketRestTemplate.getRestTemplate();
    }

    public Response<Void> put(String path, String text, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> request = new HttpEntity<>(text, headers);
        try {
            putRequest(bucketWebClient, "/v1/asset/" + path, request);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    private void putRequest(RestTemplate webClient, String path, HttpEntity<?> request) {
        String url = createUrl(webClient, path);
        webClient.put(url, request);
    }

    public Response<String> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            return Response.withData(bucketWebClient.getForObject("/v1/asset/" + path, String.class, request));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return Response.withError(new Error<>(404, "Not Found"));
            }
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }
}
