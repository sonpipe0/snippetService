package com.printScript.snippetService.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.printScript.snippetService.services.ConfigService;
import com.printScript.snippetService.utils.TokenUtils;

import DTO.LintingConfigDTO;

@RestController
@RequestMapping("/lint")
public class LintConfigController {

    @Autowired
    private ConfigService configService;

    @PutMapping
    public void putLintingConfig(@RequestBody LintingConfigDTO lintingConfigDTO,
            @RequestHeader("Authorization") String token) throws IOException {
        Map<String, String> decoded = TokenUtils.decodeToken(token.substring(7));
        String userId = decoded.get("userId");
        configService.putLintingConfig(lintingConfigDTO, userId, token);
    }

    @GetMapping
    public void getLintingConfig(@RequestHeader("Authorization") String token) {
        Map<String, String> decoded = TokenUtils.decodeToken(token.substring(7));
        String userId = decoded.get("userId");
        configService.getLintingConfig(userId, token);
    }
}
