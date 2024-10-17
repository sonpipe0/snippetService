package com.printScript.snippetService.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class WebClientService {

    private WebClient webClient;
    private final String permissionsUrl;
    private final String printScriptUrl;

    public WebClientService() {
        this.permissionsUrl = System.getenv("PERMISSIONS_SERVICE_URL");
        this.printScriptUrl = System.getenv("PRINT_SCRIPT_SERVICE_URL");
        this.webClient = WebClient.builder().build();
    }

    public WebClientService permissionsWebClient() {
        this.webClient = WebClient.builder().baseUrl(permissionsUrl).build();
        return this;
    }

    public WebClientService printScriptWebClient() {
        this.webClient = WebClient.builder().baseUrl(printScriptUrl).build();
        return this;
    }

    public  Mono<JsonNode> get(
            String path,
            Consumer<HttpHeaders> headers,
            Function<WebClientResponseException, Mono<JsonNode>> errorHandler
    ) {
        return webClient.get()
                .uri(path)
                .headers(headers)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, errorHandler);
    }

    public <T> Mono<JsonNode> postObject(
            String path,
            T body,
            Consumer<HttpHeaders> headers,
            Function<WebClientResponseException, Mono<JsonNode>> errorHandler
    ) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, errorHandler);
    }


    public Mono<JsonNode> uploadMultipart(
            String path,
            MultiValueMap<String, Object> multipartData,
            Consumer<HttpHeaders> headers,
            Function<WebClientResponseException, Mono<JsonNode>> errorHandler
    ) {
        return webClient.post()
                .uri(path)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(headers)
                .bodyValue(multipartData)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, errorHandler);
    }
}
