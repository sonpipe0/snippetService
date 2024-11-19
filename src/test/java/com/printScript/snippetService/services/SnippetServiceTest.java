package com.printScript.snippetService.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.TestSecurityConfig;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducer;
import com.printScript.snippetService.redis.StatusConsumer;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.handlers.BucketHandler;
import com.printScript.snippetService.web.handlers.PermissionsManagerHandler;
import com.printScript.snippetService.web.handlers.PrintScriptServiceHandler;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class SnippetServiceTest {

    @MockBean
    private BucketHandler bucketHandler;

    @MockBean
    private PrintScriptServiceHandler printScriptServiceHandler;

    @MockBean
    private PermissionsManagerHandler permissionsManagerHandler;

    @MockBean
    private LintProducer lintProducer;

    @MockBean
    private StatusConsumer statusConsumer;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private SnippetService snippetService;

    private final Pattern uuid = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    private String mockToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"mockUserId\",\"username\":\"mockUsername\",\"role\":\"user\",\"iat\":1609459200}";
        String signature = "mockSignature";

        mockToken = base64Encode(header) + "." + base64Encode(payload) + "." + signature;
        mockToken = "Bearer " + mockToken;

        when(jwt.getTokenValue()).thenReturn(mockToken);
        when(jwt.getClaim("sub")).thenReturn("mockUserId");
        when(jwt.getClaim("username")).thenReturn("mockUsername");
        when(jwt.getClaim("role")).thenReturn("user");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContextHolder.setContext(securityContext);

        when(printScriptServiceHandler.validateCode(anyString(), anyString(), anyString()))
                .thenReturn(Response.withData("Code validated successfully"));

        when(bucketHandler.put(anyString(), anyString(), anyString())).thenReturn(Response.withData(null));

        when(permissionsManagerHandler.getSnippetAuthor(anyString(), anyString()))
                .thenReturn(Response.withData("mockUsername"));

        doReturn(Response.withData(null)).when(permissionsManagerHandler).saveRelation(anyString(), anyString(),
                anyString());

        when(permissionsManagerHandler.checkPermissions(anyString(), anyString(), anyString()))
                .thenReturn(Response.withData(null));
    }

    private String base64Encode(String value) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
    }

    @Test
    void createSnippetSuccess() {
        SnippetDTO snippetDTO = new SnippetDTO();
        snippetDTO.setCode("print('Hello World!')");
        snippetDTO.setLanguage("printscript");
        snippetDTO.setExtension("ps");
        snippetDTO.setDescription("Hello World in PrintScript");
        snippetDTO.setTitle("Hello World");

        Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

        assertNotNull(response.getData());
        assertEquals("mockUsername", response.getData().getAuthor());
        assertTrue(uuid.matcher(response.getData().getId()).matches());
        assertEquals("print('Hello World!')", response.getData().getCode());
        assertEquals("printscript", response.getData().getLanguage());
        assertEquals("ps", response.getData().getExtension());

        // test db
        Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
        assertEquals(snippet.getTitle(), "Hello World");
        assertEquals(snippet.getDescription(), "Hello World in PrintScript");
        assertEquals(snippet.getLanguage(), "printscript");
        assertEquals(snippet.getExtension(), "ps");
        assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
        assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);
    }

  @Test
  void updateSnippetTest() {
    when(bucketHandler.get(anyString(), anyString()))
        .thenReturn(Response.withData("print('Hello World!')"));

    SnippetDTO snippetDTO = new SnippetDTO();
    snippetDTO.setCode("print('Hello World!')");
    snippetDTO.setLanguage("printscript");
    snippetDTO.setExtension("ps");
    snippetDTO.setDescription("Hello World in PrintScript");
    snippetDTO.setTitle("Hello World");

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

    assertNotNull(response.getData());
    assertEquals("mockUsername", response.getData().getAuthor());
    assertTrue(uuid.matcher(response.getData().getId()).matches());
    assertEquals("print('Hello World!')", response.getData().getCode());
    assertEquals("printscript", response.getData().getLanguage());
    assertEquals("ps", response.getData().getExtension());

    // test db
    Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
    assertEquals(snippet.getTitle(), "Hello World");
    assertEquals(snippet.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet.getLanguage(), "printscript");
    assertEquals(snippet.getExtension(), "ps");
    assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);

    UpdateSnippetDTO snippetDTO2 = new UpdateSnippetDTO();
    snippetDTO2.setCode("print('Hello World!')");
    snippetDTO2.setLanguage("printscript");
    snippetDTO2.setExtension("ps");
    snippetDTO2.setDescription("Hello World in PrintScript2");
    snippetDTO2.setTitle("Hello World2");
    snippetDTO2.setSnippetId(response.getData().getId());

    Response<SnippetCodeDetails> response2 = snippetService.updateSnippet(snippetDTO2, mockToken);
    assertEquals("mockUsername", response2.getData().getAuthor());
    assertTrue(uuid.matcher(response2.getData().getId()).matches());
    assertEquals("print('Hello World!')", response2.getData().getCode());
    assertEquals("printscript", response2.getData().getLanguage());
    assertEquals("ps", response2.getData().getExtension());

    // test db
    Snippet snippet2 = snippetRepository.findById(response2.getData().getId()).get();
    assertEquals(snippet2.getTitle(), "Hello World2");
    assertEquals(snippet2.getDescription(), "Hello World in PrintScript2");
    assertEquals(snippet2.getLanguage(), "printscript");
    assertEquals(snippet2.getExtension(), "ps");
    assertEquals(snippet2.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet2.getFormatStatus(), Snippet.Status.IN_PROGRESS);
  }

  @Test
  void getSnippetDetailsTest() {

    when(bucketHandler.get(anyString(), anyString()))
        .thenReturn(Response.withData("print('Hello World!')"));

    SnippetDTO snippetDTO = new SnippetDTO();
    snippetDTO.setCode("print('Hello World!')");
    snippetDTO.setLanguage("printscript");
    snippetDTO.setExtension("ps");
    snippetDTO.setDescription("Hello World in PrintScript");
    snippetDTO.setTitle("Hello World");

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

    assertNotNull(response.getData());
    assertEquals("mockUsername", response.getData().getAuthor());
    assertTrue(uuid.matcher(response.getData().getId()).matches());
    assertEquals("print('Hello World!')", response.getData().getCode());
    assertEquals("printscript", response.getData().getLanguage());
    assertEquals("ps", response.getData().getExtension());

    // test db
    Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
    assertEquals(snippet.getTitle(), "Hello World");
    assertEquals(snippet.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet.getLanguage(), "printscript");
    assertEquals(snippet.getExtension(), "ps");
    assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);

    Response<SnippetCodeDetails> response2 =
        snippetService.getSnippetDetails(response.getData().getId(), mockToken);
    assertEquals("mockUsername", response2.getData().getAuthor());
    assertTrue(uuid.matcher(response2.getData().getId()).matches());
    assertEquals("print('Hello World!')", response2.getData().getCode());
    assertEquals("printscript", response2.getData().getLanguage());
    assertEquals("ps", response2.getData().getExtension());
  }

  @Test
  void testDeleteSnippet() {
    when(bucketHandler.get(anyString(), anyString()))
        .thenReturn(Response.withData("print('Hello World!')"));

    when(bucketHandler.delete(anyString(), anyString())).thenReturn(Response.withData(null));

    when(permissionsManagerHandler.deleteRelation(anyString(), anyString(), anyString()))
        .thenReturn(Response.withData(null));

    SnippetDTO snippetDTO = new SnippetDTO();
    snippetDTO.setCode("print('Hello World!')");
    snippetDTO.setLanguage("printscript");
    snippetDTO.setExtension("ps");
    snippetDTO.setDescription("Hello World in PrintScript");
    snippetDTO.setTitle("Hello World");

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

    assertNotNull(response.getData());
    assertEquals("mockUsername", response.getData().getAuthor());
    assertTrue(uuid.matcher(response.getData().getId()).matches());
    assertEquals("print('Hello World!')", response.getData().getCode());
    assertEquals("printscript", response.getData().getLanguage());
    assertEquals("ps", response.getData().getExtension());

    // test db
    Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
    assertEquals(snippet.getTitle(), "Hello World");
    assertEquals(snippet.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet.getLanguage(), "printscript");
    assertEquals(snippet.getExtension(), "ps");
    assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);

    Response<String> response2 =
        snippetService.deleteSnippet(response.getData().getId(), mockToken);
    assertFalse(response2.isError());

    // test db
    assertFalse(snippetRepository.findById(response.getData().getId()).isPresent());
  }

  @Test
  void testDeleteSnippetRelationship() {
    when(bucketHandler.get(anyString(), anyString()))
        .thenReturn(Response.withData("print('Hello World!')"));

    when(bucketHandler.delete(anyString(), anyString())).thenReturn(Response.withData(null));

    when(permissionsManagerHandler.deleteRelation(anyString(), anyString(), anyString()))
        .thenReturn(Response.withData(null));

    SnippetDTO snippetDTO = new SnippetDTO();
    snippetDTO.setCode("print('Hello World!')");
    snippetDTO.setLanguage("printscript");
    snippetDTO.setExtension("ps");
    snippetDTO.setDescription("Hello World in PrintScript");
    snippetDTO.setTitle("Hello World");

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

    assertNotNull(response.getData());
    assertEquals("mockUsername", response.getData().getAuthor());
    assertTrue(uuid.matcher(response.getData().getId()).matches());
    assertEquals("print('Hello World!')", response.getData().getCode());
    assertEquals("printscript", response.getData().getLanguage());
    assertEquals("ps", response.getData().getExtension());

    // test db
    Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
    assertEquals(snippet.getTitle(), "Hello World");
    assertEquals(snippet.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet.getLanguage(), "printscript");
    assertEquals(snippet.getExtension(), "ps");
    assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);

    when(permissionsManagerHandler.checkPermissions(
            eq(snippet.getId()), eq(mockToken), eq("/snippets/can-edit")))
        .thenReturn(
            Response.withError(
                new Error<>(403, "You do not have permission to delete this snippet")));

    Response<String> response2 =
        snippetService.deleteSnippet(response.getData().getId(), mockToken);
    assertFalse(response2.isError());

    // test db
    assertTrue(snippetRepository.findById(response.getData().getId()).isPresent());
  }

  @Test
  void shareSnippetTest() {
    when(bucketHandler.get(anyString(), anyString()))
        .thenReturn(Response.withData("print('Hello World!')"));

    SnippetDTO snippetDTO = new SnippetDTO();
    snippetDTO.setCode("print('Hello World!')");
    snippetDTO.setLanguage("printscript");
    snippetDTO.setExtension("ps");
    snippetDTO.setDescription("Hello World in PrintScript");
    snippetDTO.setTitle("Hello World");

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, mockToken);

    assertNotNull(response.getData());
    assertEquals("mockUsername", response.getData().getAuthor());
    assertTrue(uuid.matcher(response.getData().getId()).matches());
    assertEquals("print('Hello World!')", response.getData().getCode());
    assertEquals("printscript", response.getData().getLanguage());
    assertEquals("ps", response.getData().getExtension());

    // test db
    Snippet snippet = snippetRepository.findById(response.getData().getId()).get();
    assertEquals(snippet.getTitle(), "Hello World");
    assertEquals(snippet.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet.getLanguage(), "printscript");
    assertEquals(snippet.getExtension(), "ps");
    assertEquals(snippet.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet.getFormatStatus(), Snippet.Status.IN_PROGRESS);

    ShareSnippetDTO shareSnippetDTO = new ShareSnippetDTO(response.getData().getId(), "myFriend");

    when(permissionsManagerHandler.shareSnippet(anyString(), eq(shareSnippetDTO), anyString()))
        .thenReturn(Response.withData(null));

    Response<SnippetCodeDetails> response2 =
        snippetService.shareSnippet(shareSnippetDTO, mockToken);
    assertEquals("mockUsername", response2.getData().getAuthor());
    assertTrue(uuid.matcher(response2.getData().getId()).matches());
    assertEquals("print('Hello World!')", response2.getData().getCode());
    assertEquals("printscript", response2.getData().getLanguage());
    assertEquals("ps", response2.getData().getExtension());

    // test db
    Snippet snippet2 = snippetRepository.findById(response2.getData().getId()).get();
    assertEquals(snippet2.getTitle(), "Hello World");
    assertEquals(snippet2.getDescription(), "Hello World in PrintScript");
    assertEquals(snippet2.getLanguage(), "printscript");
    assertEquals(snippet2.getExtension(), "ps");
    assertEquals(snippet2.getLintStatus(), Snippet.Status.IN_PROGRESS);
    assertEquals(snippet2.getFormatStatus(), Snippet.Status.IN_PROGRESS);
  }
}
