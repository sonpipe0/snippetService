package com.printScript.snippetService.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

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
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.repositories.TestRepository;
import com.printScript.snippetService.web.handlers.BucketHandler;
import com.printScript.snippetService.web.handlers.PermissionsManagerHandler;
import com.printScript.snippetService.web.handlers.PrintScriptServiceHandler;

import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestServiceTests {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private SnippetRepository snippetRepository;

    @MockBean
    private BucketHandler bucketHandler;

    @MockBean
    private PermissionsManagerHandler permissionsManagerHandler;

    @MockBean
    private PrintScriptServiceHandler printScriptServiceHandler;

    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    @Autowired
    private TestService testService;

    private String token;

    private String snippetId;

    private final Pattern uuid = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    @BeforeEach
    public void setUp() {
        token = SnippetServiceTest.securityConfig(this);
        Snippet snippet = new Snippet();
        snippet.setTitle("Test snippet");
        snippet.setLanguage("printscript");
        snippet.setExtension("ps");
        snippet.setFormatStatus(Snippet.Status.IN_PROGRESS);
        snippet.setLintStatus(Snippet.Status.IN_PROGRESS);
        snippet.setTests(List.of());

        snippetRepository.save(snippet);
        snippetId = snippet.getId();
    }

    @Test
    @Transactional
    void testCreateTest() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(snippetId);
        testDTO.setTitle("Test title");
        testDTO.setInputQueue(List.of());
        testDTO.setOutputQueue(List.of("Hello, World!"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        Response<String> response = testService.createTest(testDTO, token);

        assert response.getData() != null;
        assertTrue(uuid.matcher(response.getData()).matches());

        // Check if the test was saved
        assertEquals(1, testRepository.count());
        com.printScript.snippetService.entities.Test test = testRepository.findAll().getFirst();
        assertEquals(testDTO.getTitle(), test.getTitle());
        assertEquals(testDTO.getInputQueue(), List.of());
        assertEquals(testDTO.getOutputQueue(), List.of("Hello, World!"));
    }

    @Test
    @Transactional
    void testGetTestsForSnippet() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(snippetId);
        testDTO.setTitle("Test title");
        testDTO.setInputQueue(List.of());
        testDTO.setOutputQueue(List.of("Hello, World!"));

        TestDTO testDTO2 = new TestDTO();
        testDTO2.setId(snippetId);
        testDTO2.setTitle("Test title 2");
        testDTO2.setInputQueue(List.of());
        testDTO2.setOutputQueue(List.of("Hello, World! 2"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access"))
                .thenReturn(Response.withData(null));

        testService.createTest(testDTO, token);
        testService.createTest(testDTO2, token);

        Response<List<TestDTO>> response = testService.getTestsForSnippet(snippetId, token);

        assert response.getData() != null;
        assertEquals(2, response.getData().size());

        TestDTO test1 = response.getData().getFirst();
        assertEquals(testDTO.getTitle(), test1.getTitle());
        assertEquals(testDTO.getInputQueue(), test1.getInputQueue());
        assertEquals(testDTO.getOutputQueue(), test1.getOutputQueue());

        TestDTO test2 = response.getData().getLast();
        assertEquals(testDTO2.getTitle(), test2.getTitle());
        assertEquals(testDTO2.getInputQueue(), test2.getInputQueue());
        assertEquals(testDTO2.getOutputQueue(), test2.getOutputQueue());
    }

    @Test
    @Transactional
    void testUpdateTest() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(snippetId);
        testDTO.setTitle("Test title");
        testDTO.setInputQueue(List.of());
        testDTO.setOutputQueue(List.of("Hello, World!"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        Response<String> response = testService.createTest(testDTO, token);

        assert response.getData() != null;
        assertTrue(uuid.matcher(response.getData()).matches());

        // Check if the test was saved
        assertEquals(1, testRepository.count());
        com.printScript.snippetService.entities.Test test = testRepository.findAll().getFirst();
        assertEquals(testDTO.getTitle(), test.getTitle());
        assertEquals(testDTO.getInputQueue(), List.of());
        assertEquals(testDTO.getOutputQueue(), List.of("Hello, World!"));

        TestDTO updatedTestDTO = new TestDTO();
        updatedTestDTO.setId(test.getId());
        updatedTestDTO.setTitle("Updated test title");
        updatedTestDTO.setInputQueue(List.of());
        updatedTestDTO.setOutputQueue(List.of("Hello, World!"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        Response<Void> updatedResponse = testService.updateTest(updatedTestDTO, token);

        assertFalse(updatedResponse.isError());

        // Check if the test was saved
        assertEquals(1, testRepository.count());
        com.printScript.snippetService.entities.Test updatedTest = testRepository.findAll().getFirst();
        assertEquals(updatedTestDTO.getTitle(), updatedTest.getTitle());
        assertEquals(updatedTestDTO.getInputQueue(), List.of());
        assertEquals(updatedTestDTO.getOutputQueue(), List.of("Hello, World!"));
    }

    @Test
    @Transactional
    void testRun() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(snippetId);
        testDTO.setTitle("Test title");
        testDTO.setInputQueue(List.of());
        testDTO.setOutputQueue(List.of("Hello, World!"));

        TestDTO testDTO2 = new TestDTO();
        testDTO2.setId(snippetId);
        testDTO2.setTitle("Test title 2");
        testDTO2.setInputQueue(List.of());
        testDTO2.setOutputQueue(List.of("Hello, World2"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access"))
                .thenReturn(Response.withData(null));

        Response<String> response = testService.createTest(testDTO, token);

        when(printScriptServiceHandler.executeTest(snippetId, "1.1", List.of(), List.of("Hello, World!"), token))
                .thenReturn(Response.withData(null));

        Response<Void> runResponse = testService.runTest(testDTO, token);

        assertFalse(runResponse.isError());

        when(printScriptServiceHandler.executeTest(snippetId, "1.1", List.of(), List.of("Hello, World2"), token))
                .thenReturn(Response.withError(new Error<>(500, "Internal server error")));

        Response<Void> runResponse2 = testService.runTest(testDTO2, token);
        assertTrue(runResponse2.isError());
    }

    @Test
    @Transactional
    void testDelete() {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(snippetId);
        testDTO.setTitle("Test title");
        testDTO.setInputQueue(List.of());
        testDTO.setOutputQueue(List.of("Hello, World!"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        Response<String> response = testService.createTest(testDTO, token);

        assert response.getData() != null;
        assertTrue(uuid.matcher(response.getData()).matches());

        // Check if the test was saved
        assertEquals(1, testRepository.count());
        com.printScript.snippetService.entities.Test test = testRepository.findAll().getFirst();
        assertEquals(testDTO.getTitle(), test.getTitle());
        assertEquals(testDTO.getInputQueue(), List.of());
        assertEquals(testDTO.getOutputQueue(), List.of("Hello, World!"));

        when(permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit"))
                .thenReturn(Response.withData(null));

        Response<Void> deleteResponse = testService.deleteTest(test.getId(), token);

        assertFalse(deleteResponse.isError());

        assertEquals(0, testRepository.count());
    }
}
