package com.printScript.snippetService.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {
    private final Logger log = LoggerFactory.getLogger(RestTemplateService.class);

    private RestTemplate restTemplate;

    private final String permissionsUrl;
    private final String printScriptUrl;
    private final String bucketUrl;

    public RestTemplateService(RestTemplateBuilder restTemplateBuilder) {
        log.info("RestTemplateService was called");
        this.permissionsUrl = System.getenv("PERMISSIONS_SERVICE_URL");
        this.printScriptUrl = System.getenv("PRINT_SCRIPT_SERVICE_URL");
        this.bucketUrl = System.getenv("BUCKET_SERVICE_URL");
        this.restTemplate = restTemplateBuilder.build(); // Build a default RestTemplate
    }

    public RestTemplateService permissionsRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("permissionsRestTemplate was called");
        this.restTemplate = restTemplateBuilder.rootUri(permissionsUrl).build();
        return this;
    }

    public RestTemplateService printScriptRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("printScriptRestTemplate was called");
        this.restTemplate = restTemplateBuilder.rootUri(printScriptUrl).build();
        return this;
    }

    public RestTemplateService bucketRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("bucketRestTemplate was called");
        this.restTemplate = restTemplateBuilder.rootUri(bucketUrl).build();
        return this;
    }

    public RestTemplate getRestTemplate() {
        log.info("getRestTemplate was called");
        return restTemplate;
    }
}
