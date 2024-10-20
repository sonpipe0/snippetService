package com.printScript.snippetService.config;

import com.printScript.snippetService.services.RestTemplateService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplateService printScriptRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplateService restTemplateService = new RestTemplateService(builder);
        restTemplateService.printScriptRestTemplate(builder);
        return restTemplateService;
    }

    @Bean
    public RestTemplateService permissionsRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplateService restTemplateService = new RestTemplateService(builder);
        restTemplateService.permissionsRestTemplate(builder);
        return restTemplateService;
    }
}
