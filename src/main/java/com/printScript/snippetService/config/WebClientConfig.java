package com.printScript.snippetService.config;

import com.printScript.snippetService.services.WebClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClientService printScriptWebClient() {
        return new WebClientService().printScriptWebClient();
    }

    @Bean
    public WebClientService permissionsWebClient() {
        return new WebClientService().permissionsWebClient();
    }
}
