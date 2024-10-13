package com.printScript.snippetService.services;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

    private WebClient webClient;
    private final String permissionsUrl;
    private final String printScriptUrl;

    public WebClientService(Dotenv dotenv) {
        this.permissionsUrl = dotenv.get("PERMISSIONS_SERVICE_URL");
        this.printScriptUrl = dotenv.get("PRINT_SCRIPT_SERVICE_URL");
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

    public <T, W> Mono<W> post(String path, T body, Class<W> response) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(response);
    }

    public <T> Mono<T> get(String path, Class<T> response) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(response);
    }
}
