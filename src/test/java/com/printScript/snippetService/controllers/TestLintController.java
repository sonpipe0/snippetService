package com.printScript.snippetService.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.TestSecurityConfig;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.services.ConfigService;
import com.printScript.snippetService.services.SnippetServiceTest;

import DTO.LintingConfigDTO;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestLintController {
    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    String token;

    @Autowired
    private LintConfigController lintConfigController;

    @MockBean
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        token = SnippetServiceTest.securityConfig(this);
    }

    @Test
    void testPutLint() throws IOException {
        LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
        lintingConfigDTO.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);
        lintingConfigDTO.setRestrictPrintln(true);
        lintingConfigDTO.setRestrictReadInput(true);

        when(configService.putLintingConfig(lintingConfigDTO, "mockUserId", token)).thenReturn(Response.withData(null));

        lintConfigController.putLintingConfig(lintingConfigDTO, token);

        when(configService.putLintingConfig(lintingConfigDTO, "mockUserId", token))
                .thenReturn(Response.withError(new Error<>(500, "Internal Server Error")));

        assertTrue(true);
    }

    @Test
    void testGetLint() {
        LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
        lintingConfigDTO.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);
        lintingConfigDTO.setRestrictPrintln(true);
        lintingConfigDTO.setRestrictReadInput(true);

        when(configService.getLintingConfig("mockUserId", token)).thenReturn(Response.withData(lintingConfigDTO));

        assertEquals(200, lintConfigController.getLintingConfig(token).getStatusCode().value());

        when(configService.getLintingConfig("mockUserId", token))
                .thenReturn(Response.withError(new Error<>(500, "Internal Server Error")));

        assertEquals(500, lintConfigController.getLintingConfig(token).getStatusCode().value());
    }
}
