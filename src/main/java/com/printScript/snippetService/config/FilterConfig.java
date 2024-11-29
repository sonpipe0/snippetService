package com.printScript.snippetService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.printScript.snippetService.filters.CorrelationIdFilter;

@Configuration
public class FilterConfig {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}
