package com.printScript.snippetService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.printScript.snippetService.redis.LintProducerInterface;

@Configuration
public class TestConfig {
    @Bean
    @Primary
    public LintProducerInterface spyLintProducer() {
        return new SpyLintProducer();
    }
}
