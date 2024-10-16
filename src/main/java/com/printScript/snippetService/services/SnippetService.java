package com.printScript.snippetService.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.SnippetRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private WebClientService permissionsWebClient;

    @Autowired
    private WebClientService printScriptWebClient;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    public Response<String> saveSnippet(Map<String, Object> postFile) {
        try {
            String code = (String) postFile.get("file");
            String userId = (String) postFile.get("userId");
            String token = (String) postFile.get("token");

            Snippet snippet = new Snippet();
            snippet.setSnippet(code.getBytes());
            String snippetId = snippet.getId();


            HashMap<String, String> body = new HashMap<>();
            body.put("snippetId", snippetId);
            body.put("userId", userId);

            Mono<JsonNode> serverResponse = permissionsWebClient
                    .postObject("/snippet/load/relationship", body, httpHeaders -> {
                        httpHeaders.setBearerAuth(token);
                        httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    }, error -> {
                        Response<Error> errorResponse = Response.errorFromWebFluxError(error);
                        JsonNode errorNode = jacksonObjectMapper.valueToTree(errorResponse);
                        return Mono.just(errorNode);
                    });
            JsonNode responseNode = serverResponse.block();

            assert responseNode != null;
            if (responseNode.has("error")) {
                return Response.withError(jacksonObjectMapper.treeToValue(responseNode.get("error"), Error.class));
            }
            else {
                snippetRepository.save(snippet);
            }
            return Response.withData(snippetId);
        } catch (Exception e) {
            return Response.withError(new Error(500, "Internal server error"));
        }
    }

    @Transactional
    public Response<String> saveFromMultiPart(Map<String, Object> body) {
        try {
            MultipartFile file = (MultipartFile) body.get("file");
            String title = (String) body.get("title");
            String description = (String) body.get("description");
            String language = (String) body.get("language");
            String userId = (String) body.get("userId");
            String token = (String) body.get("token");

            Snippet snippet = new Snippet();
            snippet.setSnippet(file.getBytes());
            snippet.setTitle(title);
            snippet.setDescription(description);
            snippet.setLanguage(language);
            snippetRepository.save(snippet);


            String snippetId = snippet.getId();

            JsonNode response = permissionsWebClient.postObject("/snippets/save/relationship", Map.of("snippetId", snippetId, "userId", userId), httpHeaders -> {
                httpHeaders.set("Authorization", token);
                httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            }, error -> {
                Response<Error> errorResponse = Response.errorFromWebFluxError(error);
                return Mono.just(jacksonObjectMapper.valueToTree(errorResponse));
            }).block();

            if(response == null) {
                Optional<Snippet> savedSnippet = snippetRepository.findById(snippetId);
                if (savedSnippet.isEmpty()) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Response.withError(new Error(500, "Internal server error"));
                }
                return Response.withData(snippetId);
            }
            if (response.has("error")) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Response.withError(jacksonObjectMapper.treeToValue(response.get("error"), Error.class));
            }
            return Response.withData(snippetId);

        } catch (Exception e) {
            return Response.withError(new Error(500, "Internal server error"));
        }
    }

    public Response<String> getSnippet(String snippetId) {
        try {
            Optional<Snippet> snippet = snippetRepository.findById(snippetId);
            if (snippet.isEmpty()) {
                Error error = new Error(404, "Snippet not found");
                return Response.withError(error);
            }
            String file = new String(snippet.get().getSnippet());
            return Response.withData(file);
        } catch (Exception e) {
            Error error = new Error(500, "Internal server error");
            return Response.withError(error);
        }
    }

    public Response<String> deleteSnippet(String snippetId) {
        boolean exists = snippetRepository.existsById(snippetId);
        if (!exists) {
            return Response.withError(new Error(404, "Snippet not found"));
        }

        snippetRepository.deleteById(snippetId);
        return Response.withData(snippetId);
    }
}
