package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.errorDTO.ErrorMessage;
import com.printScript.snippetService.repositories.SnippetRepository;

import jakarta.transaction.Transactional;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SnippetService(RestTemplateService permissionsRestTemplate, RestTemplateService printScriptRestTemplate,
            ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        String userId = snippetDTO.getUserId();
        String code = snippetDTO.getCode();
        String version = snippetDTO.getVersion();

        Snippet snippet = new Snippet();
        snippet.setSnippet(code.getBytes());
        snippet.setTitle(snippetDTO.getTitle());
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(snippetDTO.getLanguage());
        snippet.setVersion(version);

        if (!snippet.isValid()) {
            List<String> invalidFields = snippet.getInvalidFields();
            String message = "Invalid body: " + String.join(", ", invalidFields)
                    + (invalidFields.size() > 1 ? " are required" : " is required");
            return Response.withError(new Error<>(400, message));
        }
        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        String snippetId = snippet.getId();

        Response<String> permissionsResponse = saveRelation(token, userId, snippetId);
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> printScriptResponse = validateCode(code, version);
        if (printScriptResponse.isError()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return printScriptResponse;
        }

        return Response.withData(snippetId);
    }

    @Transactional
    public Response<String> saveSnippetFile(MultipartFile file, SnippetInfoDTO snippetInfoDTO, String token) {
        try {
            String userId = snippetInfoDTO.getUserId();
            String version = snippetInfoDTO.getVersion();

            Snippet snippet = new Snippet();
            snippet.setSnippet(file.getBytes());
            snippet.setTitle(snippetInfoDTO.getTitle());
            snippet.setDescription(snippetInfoDTO.getDescription());
            snippet.setLanguage(snippetInfoDTO.getLanguage());
            snippetRepository.save(snippet);

            String snippetId = snippet.getId();

            Response<String> permissionsResponse = saveRelation(token, userId, snippetId);
            if (permissionsResponse.isError())
                return permissionsResponse;

            Response<String> printScriptResponse = validateFileCode(file, version);
            if (printScriptResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return printScriptResponse;
            }

            return Response.withData(snippetId);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }

    public Response<String> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        String snippetId = updateSnippetDTO.getSnippetId();
        String userId = updateSnippetDTO.getUserId();
        String code = updateSnippetDTO.getCode();
        String version = updateSnippetDTO.getVersion();

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token);
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> printScriptResponse = validateCode(code, version);
        if (printScriptResponse.isError())
            return printScriptResponse;

        Snippet snippet = snippetOptional.get();
        snippet.setSnippet(code.getBytes());
        snippet.setTitle(updateSnippetDTO.getTitle());
        snippet.setDescription(updateSnippetDTO.getDescription());
        snippet.setLanguage(updateSnippetDTO.getLanguage());
        snippet.setVersion(version);
        snippetRepository.save(snippet);

        return Response.withData("Snippet updated successfully");
    }

    public Response<String> updateSnippetFile(MultipartFile file, UpdateSnippetInfoDTO updateSnippetInfoDTO,
            String token) {
        try {
            String snippetId = updateSnippetInfoDTO.getSnippetId();
            String version = updateSnippetInfoDTO.getVersion();
            String userId = updateSnippetInfoDTO.getUserId();

            Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
            if (snippetOptional.isEmpty()) {
                return Response.withError(new Error<>(404, "Snippet not found"));
            }

            Response<String> permissionsResponse = checkPermissions(snippetId, userId, token);
            if (permissionsResponse.isError())
                return permissionsResponse;

            Response<String> printScriptResponse = validateFileCode(file, version);
            if (printScriptResponse.isError())
                return printScriptResponse;

            Snippet snippet = snippetOptional.get();
            snippet.setSnippet(file.getBytes());
            snippet.setTitle(updateSnippetInfoDTO.getTitle());
            snippet.setDescription(updateSnippetInfoDTO.getDescription());
            snippet.setLanguage(updateSnippetInfoDTO.getLanguage());
            snippet.setVersion(version);
            snippetRepository.save(snippet);

            return Response.withData("Snippet updated successfully");
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }

    public Response<SnippetDetails> getSnippetDetails(String snippetId, String userId, String token) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = checkPermissions(snippetId, userId, token);
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetOpt.get();

        SnippetDetails snippetDetails = new SnippetDetails(snippet.getTitle(), snippet.getDescription(),
                snippet.getLanguage(), snippet.getVersion(), new String(snippet.getSnippet(), StandardCharsets.UTF_8));

        return Response.withData(snippetDetails);
    }

    private Response<String> saveRelation(String token, String userId, String snippetId) {
        HttpEntity<Permissions> requestPermissions = createPostPermissionsRequest(userId, snippetId, token);
        try {
            postRequest(permissionsWebClient, "/snippets/save/relationship", requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    private Response<String> checkPermissions(String snippetId, String userId, String token) {
        HttpEntity<Void> requestPermissions = createGetPermissionsRequest(token);
        try {
            String path = "/snippets/hasAccess?snippetId=" + snippetId + "&userId=" + userId;
            getRequest(permissionsWebClient, path, requestPermissions, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    private Response<String> validateCode(String code, String version) {
        HttpEntity<Validation> requestPrintScript = createPrintScriptRequest(code, version);
        try {
            postRequest(printScriptWebClient, "/runner/validate", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return getValidationErrors(e);
        }
    }

    private Response<String> validateFileCode(MultipartFile file, String version) {
        try {
            HttpEntity<MultiValueMap<String, Object>> requestPrintScript = createPrintScriptFileRequest(file, version);
            try {
                postRequest(printScriptWebClient, "/runner/validate/file", requestPrintScript, Void.class);
                return Response.withData(null);
            } catch (HttpClientErrorException e) {
                return getValidationErrors(e);
            }
        } catch (Exception err) {
            return Response.withError(new Error<>(500, err.getMessage()));
        }
    }

    private Response<String> getValidationErrors(HttpClientErrorException e) {
        try {
            List<ErrorMessage> errorMessages = objectMapper.readValue(e.getResponseBodyAsString(),
                    new TypeReference<List<ErrorMessage>>() {
                    });
            return Response.withError(new Error<>(e.getStatusCode().value(), errorMessages));
        } catch (JsonProcessingException ex) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<List<SnippetDetails>> getAccessibleSnippets(String userId, String token, String relation, String nameFilter, String languageFilter, Boolean isValid, String sortBy) {
        try {
            List<Snippet> snippets = snippetRepository.findAll(); // Fetch all snippets
            List<SnippetDetails> accessibleSnippets = new ArrayList<>();

            for (Snippet snippet : snippets) {
                Response<String> permissionsResponse = checkPermissions(snippet.getId(), userId, token);
                if (permissionsResponse.isError()) continue;

                boolean isAuthor = snippet.getUserId().equals(userId);
                boolean isShared = !isAuthor;

                if ((relation.equals("author") && !isAuthor) || (relation.equals("shared") && !isShared)) continue;

                boolean matchesName = nameFilter == null || snippet.getTitle().contains(nameFilter);
                boolean matchesLanguage = languageFilter == null || snippet.getLanguage().equals(languageFilter);
                boolean matchesValidity = isValid == null || validateCode(new String(snippet.getSnippet(), StandardCharsets.UTF_8), snippet.getVersion()).isError() != isValid;

                if (matchesName && matchesLanguage && matchesValidity) {
                    SnippetDetails details = new SnippetDetails(snippet.getTitle(), snippet.getDescription(), snippet.getLanguage(), snippet.getVersion(), new String(snippet.getSnippet(), StandardCharsets.UTF_8));
                    accessibleSnippets.add(details);
                }
            }

            // Sort the list based on the sortBy parameter
            if (sortBy != null) {
                accessibleSnippets.sort((s1, s2) -> {
                    switch (sortBy) {
                        case "name":
                            return s1.title().compareTo(s2.title());
                        case "language":
                            return s1.language().compareTo(s2.language());
                        case "validity":
                            return Boolean.compare(validateCode(s1.content(), s1.version()).isError(), validateCode(s2.content(), s2.version()).isError());
                        default:
                            return 0;
                    }
                });
            }

            return Response.withData(accessibleSnippets);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }
    }



}
