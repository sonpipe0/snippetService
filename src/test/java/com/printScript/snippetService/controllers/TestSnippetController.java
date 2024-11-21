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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.TestSecurityConfig;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.services.SnippetService;
import com.printScript.snippetService.services.SnippetServiceTest;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestSnippetController {

    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    String token;

    @Autowired
    private SnippetController snippetController;

    @MockBean
    private SnippetService snippetService;

    @BeforeEach
    public void setUp() {
        token = SnippetServiceTest.securityConfig(this);
    }

    @Test
    public void testSaveSnippet() {
        SnippetDTO snippetDTO = new SnippetDTO("code", "title", "description", "language", "version");
        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.saveSnippet(snippetDTO, token)).thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController.saveSnippet(snippetDTO, token).getBody());

        when(snippetService.saveSnippet(snippetDTO, token)).thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400, snippetController.saveSnippet(snippetDTO, token).getStatusCode().value());
    }

    @Test
    void testSaveSnippetFile() {
        MultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "println(1)".getBytes());

        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.saveSnippet(new SnippetDTO("println(1)", "title", "description", "language", "version"),
                token)).thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController
                .saveSnippetFile(file, "title", "description", "language", "version", token).getBody());

        when(snippetService.saveSnippet(new SnippetDTO("println(1)", "title", "description", "language", "version"),
                token)).thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400, snippetController.saveSnippetFile(file, "title", "description", "language", "version", token)
                .getStatusCode().value());
    }

    @Test
    void testUpdate() {
        UpdateSnippetDTO snippetDTO = new UpdateSnippetDTO("code", "id1", "title", "description", "language",
                "version");
        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.updateSnippet(snippetDTO, token)).thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController.updateSnippet(snippetDTO, token).getBody());

        when(snippetService.updateSnippet(snippetDTO, token)).thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400, snippetController.updateSnippet(snippetDTO, token).getStatusCode().value());
    }

    @Test
    void testUpdateSnippetFile() {
        MultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "println(1)".getBytes());

        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.updateSnippet(
                new UpdateSnippetDTO("println(1)", "id1", "title", "description", "language", "version"), token))
                .thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController
                .updateSnippetFile(file, "id1", "title", "description", "language", "version", token).getBody());

        when(snippetService.updateSnippet(
                new UpdateSnippetDTO("println(1)", "id1", "title", "description", "language", "version"), token))
                .thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400,
                snippetController.updateSnippetFile(file, "id1", "title", "description", "language", "version", token)
                        .getStatusCode().value());
    }

    @Test
    void testGetSnippetDetails() {
        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.getSnippetDetails("id1", token)).thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController.getSnippetDetails("id1", token).getBody());

        when(snippetService.getSnippetDetails("id1", token)).thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400, snippetController.getSnippetDetails("id1", token).getStatusCode().value());
    }

  @Test
  void testDeleteSnippet() {
    when(snippetService.deleteSnippet("id1", token))
        .thenReturn(Response.withData("Snippet deleted successfully"));

    assertEquals(
        "Snippet deleted successfully", snippetController.deleteSnippet("id1", token).getBody());

    when(snippetService.deleteSnippet("id1", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(400, snippetController.deleteSnippet("id1", token).getStatusCode().value());
  }

    @Test
    void testShareSnippet() {
        ShareSnippetDTO shareSnippetDTO = new ShareSnippetDTO("id1", "user1");

        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.shareSnippet(shareSnippetDTO, token)).thenReturn(Response.withData(snippetCodeDetails));

        assertEquals(snippetCodeDetails, snippetController.shareSnippet(shareSnippetDTO, token).getBody());

        when(snippetService.shareSnippet(shareSnippetDTO, token))
                .thenReturn(Response.withError(new Error<>(400, "error")));

        assertEquals(400, snippetController.shareSnippet(shareSnippetDTO, token).getStatusCode().value());
    }

  @Test
  void testGetFormatted() {

    when(snippetService.getFormattedFile("id1", token)).thenReturn(Response.withData("println(1)"));

    assertEquals("println(1)", snippetController.getFormattedSnippet("id1", token).getBody());

    when(snippetService.getFormattedFile("id1", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));

    assertEquals(400, snippetController.getFormattedSnippet("id1", token).getStatusCode().value());
  }

    @Test
    void testGetAccessibleSnippets() {
        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);
        List<SnippetCodeDetails> snippetCodeDetailsList = List.of(snippetCodeDetails);

        when(snippetService.getAccessibleSnippets(token, null, 1, 1, null))
                .thenReturn(Response.withData(snippetCodeDetailsList));

        assertEquals(snippetCodeDetailsList,
                snippetController.getAccessibleSnippets(token, null, 1, 1, null).getBody());

        when(snippetService.getAccessibleSnippets(token, null, 1, 1, null))
                .thenReturn(Response.withError(new Error<>(400, "error")));
    }

    @Test
    void testGetSnippetUsers() {
        PaginatedUsers users = new PaginatedUsers(0, 10, 1, List.of(new User("mockUsername", "mockId")));

        when(snippetService.getSnippetUsers(token, "", 0, 10)).thenReturn(Response.withData(users));

        assertEquals(users, snippetController.getSnippetUsers(token, "", 0, 10).getBody());

        when(snippetService.getSnippetUsers(token, "", 0, 10))
                .thenReturn(Response.withError(new Error<>(400, "error")));

        assertEquals(400, snippetController.getSnippetUsers(token, "", 0, 10).getStatusCode().value());
    }

    @Test
    void testDownloadSnippet() {
        SnippetCodeDetails snippetCodeDetails = new SnippetCodeDetails("mockUsername", "mockId", "title", "description",
                "language", "version", "code", Snippet.Status.IN_PROGRESS);

        when(snippetService.downloadSnippet("id1", token))
                .thenReturn(Response.withData(new SnippetService.Tuple("println(1)", "filename.txt")));

        assertEquals("println(1)", snippetController.downloadSnippet("id1", token).getBody());

        when(snippetService.downloadSnippet("id1", token)).thenReturn(Response.withError(new Error<>(400, "error")));
        assertEquals(400, snippetController.downloadSnippet("id1", token).getStatusCode().value());
    }
}
