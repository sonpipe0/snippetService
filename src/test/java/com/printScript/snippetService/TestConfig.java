package com.printScript.snippetService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.printScript.snippetService.redis.LintProducerInterface;

@Configuration
public class TestConfig {
    @Bean(name = "testSpyLintProducer")
    public LintProducerInterface spyLintProducer() {
        return new SpyLintProducer();
    }
}
