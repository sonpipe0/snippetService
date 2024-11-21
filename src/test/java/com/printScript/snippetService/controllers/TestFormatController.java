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

import DTO.FormatConfigDTO;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestFormatController {
    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    String token;

    @Autowired
    private FormatConfigController formatConfigController;

    @MockBean
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        token = SnippetServiceTest.securityConfig(this);
    }

    @Test
    void testPutFormatConfig() throws IOException {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setLinesBeforePrintln(1);
        formatConfigDTO.setSpaceBeforeColon(false);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setSpaceAroundEquals(true);
        formatConfigDTO.setIfBraceBelowLine(false);
        when(configService.putFormatConfig(formatConfigDTO, "mockUserId", token)).thenReturn(Response.withData(null));

        formatConfigController.putFormatConfig(formatConfigDTO, token);

        assertEquals(200, formatConfigController.putFormatConfig(formatConfigDTO, token).getStatusCode().value());

        when(configService.putFormatConfig(formatConfigDTO, "mockUserId", token))
                .thenReturn(Response.withError(new Error(400, "error")));

        assertEquals(400, formatConfigController.putFormatConfig(formatConfigDTO, token).getStatusCode().value());
    }

    @Test
    void testGetFormatConfig() {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setLinesBeforePrintln(1);
        formatConfigDTO.setSpaceBeforeColon(false);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setSpaceAroundEquals(true);
        formatConfigDTO.setIfBraceBelowLine(false);
        when(configService.getFormatConfig("mockUserId", token)).thenReturn(Response.withData(formatConfigDTO));

        formatConfigController.getFormatConfig(token);

        assertEquals(formatConfigDTO, formatConfigController.getFormatConfig(token).getBody());

        when(configService.getFormatConfig("mockUserId", token))
                .thenReturn(Response.withError(new Error(400, "error")));

        assertEquals(400, formatConfigController.getFormatConfig(token).getStatusCode().value());
    }
}
