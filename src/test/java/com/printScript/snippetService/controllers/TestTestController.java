package com.printScript.snippetService.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

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
import com.printScript.snippetService.DTO.TestDTO;
import com.printScript.snippetService.TestSecurityConfig;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.services.SnippetServiceTest;
import com.printScript.snippetService.services.TestService;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestTestController {
    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    String token;

    @Autowired
    private TestController testController;

    @MockBean
    private TestService testService;

    @BeforeEach
    void setUp() {
        token = SnippetServiceTest.securityConfig(this);
    }

    @Test
    void testCreateTest() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId("snippetId");
        testDTO.setTitle("title");
        testDTO.setInputQueue(List.of("input"));
        testDTO.setOutputQueue(List.of("output"));

        when(testService.createTest(testDTO, token)).thenReturn(Response.withData("testId"));

        assertEquals("testId", testController.createTest(testDTO, token).getBody());

        when(testService.createTest(testDTO, token)).thenReturn(Response.withError(new Error<>(400, "error")));

        assertEquals(400, testController.createTest(testDTO, token).getStatusCode().value());
    }

  @Test
  void testGetTestsForSnippet() {
    when(testService.getTestsForSnippet("snippetId", token))
        .thenReturn(Response.withData(List.of()));

    assertEquals(
        200, testController.getTestsForSnippet("snippetId", token).getStatusCode().value());

    when(testService.getTestsForSnippet("snippetId", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));

    assertEquals(
        400, testController.getTestsForSnippet("snippetId", token).getStatusCode().value());
  }

    @Test
    void testUpdateTest() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId("snippetId");
        testDTO.setTitle("title");
        testDTO.setInputQueue(List.of("input"));
        testDTO.setOutputQueue(List.of("output"));

        when(testService.updateTest(testDTO, token)).thenReturn(Response.withData(null));

        assertEquals(200, testController.updateTest(testDTO, token).getStatusCode().value());

        when(testService.updateTest(testDTO, token)).thenReturn(Response.withError(new Error<>(400, "error")));

        assertEquals(400, testController.updateTest(testDTO, token).getStatusCode().value());
    }

  @Test
  void testDeleteTest() {
    when(testService.deleteTest("testId", token)).thenReturn(Response.withData(null));

    assertEquals(200, testController.deleteTest("testId", token).getStatusCode().value());

    when(testService.deleteTest("testId", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));

    assertEquals(400, testController.deleteTest("testId", token).getStatusCode().value());
  }

    @Test
    void testRunTest() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId("snippetId");
        testDTO.setTitle("title");
        testDTO.setInputQueue(List.of("input"));
        testDTO.setOutputQueue(List.of("output"));

        when(testService.runTest(testDTO, token)).thenReturn(Response.withData(null));

        assertEquals(200, testController.runTest(testDTO, token).getStatusCode().value());

        when(testService.runTest(testDTO, token)).thenReturn(Response.withError(new Error<>(400, "error")));

        assertEquals(400, testController.runTest(testDTO, token).getStatusCode().value());
    }
}
