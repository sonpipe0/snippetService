package com.printScript.snippetService.services;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {
    private RestTemplate restTemplate;

    private final String permissionsUrl;
    private final String printScriptUrl;
    private final String bucketUrl;

    public RestTemplateService(RestTemplateBuilder restTemplateBuilder) {
        this.permissionsUrl = System.getenv("PERMISSIONS_SERVICE_URL");
        this.printScriptUrl = System.getenv("PRINT_SCRIPT_SERVICE_URL");
        this.bucketUrl = System.getenv("BUCKET_SERVICE_URL");
        this.restTemplate = restTemplateBuilder.build(); // Build a default RestTemplate
    }

    public RestTemplateService permissionsRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.rootUri(permissionsUrl).build();
        return this;
    }

    public RestTemplateService printScriptRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.rootUri(printScriptUrl).build();
        return this;
    }

    public RestTemplateService bucketRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.rootUri(bucketUrl).build();
        return this;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
