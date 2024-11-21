package com.printScript.snippetService.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.repositories.FormatConfigRepository;
import com.printScript.snippetService.repositories.LintingConfigRepository;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.ConfigServiceWebHandler;
import com.printScript.snippetService.web.handlers.BucketHandler;

import DTO.FormatConfigDTO;
import DTO.LintingConfigDTO;
import Utils.FormatSerializer;
import Utils.LintSerializer;
import events.ConfigPublishEvent;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ConfigServiceTest {

    @MockBean
    private BucketHandler bucketHandler;

    @Autowired
    private LintUpdateService lintUpdateService;

    @Autowired
    private FormatUpdateService formatUpdateService;

    @Autowired
    private LintingConfigRepository lintingConfigRepository;

    @Autowired
    private FormatConfigRepository formatConfigRepository;

    @Autowired
    ConfigService configService;

    @Autowired
    SnippetRepository snippetRepository;

    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    @MockBean
    private ConfigServiceWebHandler configServiceWebHandler;

    private String token;

    private String snippetId;
    private String snippetId2;
    private String snippetId3;

    @BeforeEach
    void setUp() {
        token = SnippetServiceTest.securityConfig(this);

        Snippet snippet = new Snippet();
        snippet.setLanguage("printscript");
        snippet.setExtension("ps");
        snippet.setLintStatus(Snippet.Status.COMPLIANT);
        snippet.setFormatStatus(Snippet.Status.COMPLIANT);
        snippet.setTitle("mockSnippetTitle");
        snippet.setDescription("mockSnippetDescription");
        snippetRepository.save(snippet);

        Snippet snippet2 = new Snippet();
        snippet2.setLanguage("printscript");
        snippet2.setExtension("ps");
        snippet2.setLintStatus(Snippet.Status.COMPLIANT);
        snippet2.setFormatStatus(Snippet.Status.COMPLIANT);
        snippet2.setTitle("mockSnippetTitle2");
        snippet2.setDescription("mockSnippetDescription2");
        snippetRepository.save(snippet2);

        Snippet snippet3 = new Snippet();
        snippet3.setLanguage("python");
        snippet3.setExtension("py");
        snippet3.setLintStatus(Snippet.Status.COMPLIANT);
        snippet3.setFormatStatus(Snippet.Status.COMPLIANT);
        snippet3.setTitle("mockSnippetTitle3");
        snippet3.setDescription("mockSnippetDescription3");
        snippetRepository.save(snippet3);

        when(configServiceWebHandler.getAllSnippets(anyString(), anyString()))
                .thenReturn(Response.withData(List.of(snippet.getId(), snippet2.getId(), snippet3.getId())));

        snippetId = snippet.getId();
        snippetId2 = snippet2.getId();
        snippetId3 = snippet3.getId();
    }

    @Test
    void testPutLintingConfig() throws IOException {
        LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
        lintingConfigDTO.setRestrictPrintln(true);
        lintingConfigDTO.setRestrictReadInput(true);
        lintingConfigDTO.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);

        String expectedSerializedConfig = new LintSerializer().serialize(lintingConfigDTO);

        configService.putLintingConfig(lintingConfigDTO, "mockUserId", token);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bucketHandler).put(eq("lint/mockUserId"), captor.capture(), eq(token));
        verify(lintProducer, times(2)).publishEvent(any());

        String capturedValue = captor.getValue();
        assertEquals(expectedSerializedConfig, capturedValue);

        Snippet snippet = snippetRepository.findById(snippetId).orElse(null);
        Snippet snippet2 = snippetRepository.findById(snippetId2).orElse(null);
        Snippet snippet3 = snippetRepository.findById(snippetId3).orElse(null);

        assertEquals(Snippet.Status.IN_PROGRESS, snippet.getLintStatus());
        assertEquals(Snippet.Status.IN_PROGRESS, snippet2.getLintStatus());
        assertEquals(Snippet.Status.UNKNOWN, snippet3.getLintStatus());
    }

    @Test
    void testPutFormatConfig() throws IOException {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setIfBraceBelowLine(false);
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setSpaceAfterColon(true);
        formatConfigDTO.setLinesBeforePrintln(1);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setSpaceBeforeColon(false);

        String expectedSerializedConfig = new FormatSerializer().serialize(formatConfigDTO);

        configService.putFormatConfig(formatConfigDTO, "mockUserId", token);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bucketHandler).put(eq("format/mockUserId"), captor.capture(), eq(token));
        verify(lintProducer, times(2)).publishEvent(any());

        String capturedValue = captor.getValue();
        assertEquals(expectedSerializedConfig, capturedValue);

        Snippet snippet = snippetRepository.findById(snippetId).orElse(null);
        Snippet snippet2 = snippetRepository.findById(snippetId2).orElse(null);
        Snippet snippet3 = snippetRepository.findById(snippetId3).orElse(null);

        assertEquals(Snippet.Status.IN_PROGRESS, snippet.getFormatStatus());
        assertEquals(Snippet.Status.IN_PROGRESS, snippet2.getFormatStatus());
        assertEquals(Snippet.Status.UNKNOWN, snippet3.getFormatStatus());
    }

    @Test
    void testGetFormatConfig() {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setIfBraceBelowLine(false);
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setSpaceAfterColon(true);
        formatConfigDTO.setLinesBeforePrintln(1);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setSpaceBeforeColon(false);

        String expectedSerializedConfig = new FormatSerializer().serialize(formatConfigDTO);

        when(bucketHandler.get("format/mockUserId", token)).thenReturn(Response.withData(expectedSerializedConfig));

        Response<FormatConfigDTO> response = configService.getFormatConfig("mockUserId", token);

        assertFalse(response.isError());
        assertEquals(expectedSerializedConfig, new FormatSerializer().serialize(response.getData()));
    }

    @Test
    void testGetLintingConfig() {
        LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
        lintingConfigDTO.setRestrictPrintln(true);
        lintingConfigDTO.setRestrictReadInput(true);
        lintingConfigDTO.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);

        String expectedSerializedConfig = new LintSerializer().serialize(lintingConfigDTO);

        when(bucketHandler.get("lint/mockUserId", token)).thenReturn(Response.withData(expectedSerializedConfig));

        Response<LintingConfigDTO> response = configService.getLintingConfig("mockUserId", token);

        assertFalse(response.isError());
        assertEquals(expectedSerializedConfig, new LintSerializer().serialize(response.getData()));
    }

    @Test
    void testGenerateDefaultLintingConfig() {

        LintingConfigDTO expectedLintingConfig = new LintingConfigDTO();
        expectedLintingConfig.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);
        expectedLintingConfig.setRestrictPrintln(true);
        expectedLintingConfig.setRestrictReadInput(false);
        String lintJson = new LintSerializer().serialize(expectedLintingConfig);

        configService.generateDefaultLintingConfig("mockUserId", token);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bucketHandler).put(eq("lint/mockUserId"), captor.capture(), eq(token));

        String capturedValue = captor.getValue();
        assertEquals(lintJson, capturedValue);
    }

    @Test
    void testGenerateDefaultFormatConfig() {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setIfBraceBelowLine(false);
        formatConfigDTO.setSpaceAfterColon(true);
        formatConfigDTO.setSpaceBeforeColon(true);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setSpaceAroundEquals(true);
        formatConfigDTO.setEnforceSpacingBetweenTokens(false);
        formatConfigDTO.setLinesBeforePrintln(0);

        String formatJson = new FormatSerializer().serialize(formatConfigDTO);

        configService.generateDefaultFormatConfig("mockUserId", token);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bucketHandler).put(eq("format/mockUserId"), captor.capture(), eq(token));

        String capturedValue = captor.getValue();
        assertEquals(formatJson, capturedValue);
    }

    @Test
    void testPutViolatingLintingConfig() throws IOException {
        LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
        lintingConfigDTO.setIdentifierFormat(null);
        lintingConfigDTO.setRestrictPrintln(true);
        lintingConfigDTO.setRestrictReadInput(false);

        when(bucketHandler.put(eq("lint/mockUserId"), any(), eq(token)))
                .thenReturn(Response.withError(new Error<>(500, "Internal Server Error")));

        verify(lintProducer, never()).publishEvent(any());
        verify(bucketHandler, never()).put(any(), any(), any());
        Response<Void> response = configService.putLintingConfig(lintingConfigDTO, "mockUserId", token);

        assertTrue(response.isError());
        assertEquals(new Error<>(400, "identifier format cannot be null\n"), response.getError());
    }

    @Test
    void testExternalServerError() throws IOException {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setIndentInsideBraces(4);
        formatConfigDTO.setIfBraceBelowLine(false);
        formatConfigDTO.setSpaceAfterColon(true);
        formatConfigDTO.setSpaceBeforeColon(true);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setNewLineAfterSemicolon(true);
        formatConfigDTO.setSpaceAroundEquals(true);
        formatConfigDTO.setEnforceSpacingBetweenTokens(false);
        formatConfigDTO.setLinesBeforePrintln(0);

        LintingConfigDTO expectedLintingConfig = new LintingConfigDTO();
        expectedLintingConfig.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);
        expectedLintingConfig.setRestrictPrintln(true);
        expectedLintingConfig.setRestrictReadInput(false);

        when(bucketHandler.put(eq("lint/mockUserId"), any(), eq(token)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        when(bucketHandler.put(eq("format/mockUserId"), any(), eq(token)))
                .thenThrow(new RuntimeException("Internal Server Error"));

        ConfigPublishEvent event = new ConfigPublishEvent();
        event.setSnippetId("mockSnippetId");
        event.setType(ConfigPublishEvent.ConfigType.LINT);

        Response<Void> response = configService.putLintingConfig(expectedLintingConfig, "mockUserId", token);
        assertTrue(response.isError());
        assertEquals(new Error<>(500, "Internal Server Error"), response.getError());

        Response<Void> response2 = configService.putFormatConfig(formatConfigDTO, "mockUserId", token);
        assertTrue(response2.isError());
        assertEquals(new Error<>(500, "Internal Server Error"), response2.getError());

        Response<Void> response3 = configService.generateDefaultFormatConfig("mockUserId", token);
        assertTrue(response3.isError());
        assertEquals(new Error<>(500, "Internal Server Error"), response3.getError());

        Response<Void> response4 = configService.generateDefaultLintingConfig("mockUserId", token);
        assertTrue(response4.isError());
        assertEquals(new Error<>(500, "Internal Server Error"), response4.getError());
    }
}
