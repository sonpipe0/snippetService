package com.printScript.snippetService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.printScript.snippetService.interceptors.DefaultConfigGenerator;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private DefaultConfigGenerator defaultConfigGenerator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(defaultConfigGenerator);
    }
}
