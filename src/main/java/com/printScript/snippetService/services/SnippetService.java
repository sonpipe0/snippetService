package com.printScript.snippetService.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.response.HasPassed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
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
