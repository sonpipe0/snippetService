package com.printScript.snippetService.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.printScript.snippetService.interceptors.CorrelationIdInterceptor;
import com.printScript.snippetService.services.RestTemplateService;

@Configuration
public class RestTemplateConfig {

    private final CorrelationIdInterceptor correlationIdInterceptor = new CorrelationIdInterceptor();

    @Bean
    public RestTemplateService printScriptRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder().additionalInterceptors(correlationIdInterceptor);
        RestTemplateService restTemplateService = new RestTemplateService(builder);
        restTemplateService.printScriptRestTemplate(builder);
        return restTemplateService;
    }

    @Bean
    public RestTemplateService permissionsRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder().additionalInterceptors(correlationIdInterceptor);
        RestTemplateService restTemplateService = new RestTemplateService(builder);
        restTemplateService.permissionsRestTemplate(builder);
        return restTemplateService;
    }

    @Bean
    public RestTemplateService bucketRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder().additionalInterceptors(correlationIdInterceptor);
        RestTemplateService restTemplateService = new RestTemplateService(builder);
        restTemplateService.bucketRestTemplate(builder);
        return restTemplateService;
    }
}
