package com.printScript.snippetService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.printScript.snippetService.controllers.SnippetController;
import com.printScript.snippetService.services.RestTemplateService;
import com.printScript.snippetService.services.SnippetService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(SnippetController.class)
@AutoConfigureMockMvc
@Import(TestConfig.class)
public class RedisTests {

    @Mock
    private RestTemplateService restTemplateService;

    @MockBean
    private SnippetService snippetService;

    private SpyLintProducer spyLintProducer;

    @InjectMocks
    private SnippetController snippetController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        spyLintProducer = new SpyLintProducer();
        snippetController = new SnippetController(snippetService, spyLintProducer);
    }

    @Test
    public void testPostToV1StreamCiclon() {
        ResponseEntity<Object> responseEntity = snippetController.postToV1StreamCiclon();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Event produced successfully", responseEntity.getBody());

        assertEquals(1, spyLintProducer.events().size());
        assertEquals("ciclon", spyLintProducer.events().get(0));

        spyLintProducer.reset();
    }
}
