package com.printScript.snippetService.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.printScript.snippetService.repositories.FormatConfigRepository;
import com.printScript.snippetService.repositories.LintingConfigRepository;
import com.printScript.snippetService.services.ConfigService;
import com.printScript.snippetService.utils.TokenUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DefaultConfigGenerator implements HandlerInterceptor {

    @Autowired
    private ConfigService configService;

    @Autowired
    private FormatConfigRepository formatConfigRepository;

    @Autowired
    private LintingConfigRepository lintingConfigRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // ignore if it goes to http://localhost:8080/swagger-ui/index.html#/
        if (request.getRequestURI().matches(".*swagger-ui.*") || request.getRequestURI().matches(".*v3/api-docs.*")) {
            return true;
        }

        try {
            String token = request.getHeader("authorization").substring(7);
            String userId = TokenUtils.decodeToken(token).get("userId");
            if (formatConfigRepository.existsById(userId) && lintingConfigRepository.existsById(userId)) {
                return true;
            }
            configService.generateDefaultLintingConfig(userId, token);
            configService.generateDefaultFormatConfig(userId, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
