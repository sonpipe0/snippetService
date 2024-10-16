package com.printScript.snippetService.config;

import com.printScript.snippetService.services.WebClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClientService printScriptWebClient() {
        WebClientService webClientService = new WebClientService();
        webClientService.printScriptWebClient();
        return webClientService;
    }

    @Bean
    public WebClientService permissionsWebClient() {
        WebClientService webClientService = new WebClientService();
        webClientService.permissionsWebClient();
        return webClientService;
    }
}
