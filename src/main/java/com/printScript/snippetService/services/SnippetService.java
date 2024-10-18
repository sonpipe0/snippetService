package com.printScript.snippetService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.SnippetRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;


@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;

    @Autowired
    public SnippetService( RestTemplateService permissionsRestTemplate, RestTemplateService printScriptRestTemplate) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
    }



    @Transactional
    public Response<String> saveSnippet(Map<String, Object> postFile) {
            String code = (String) postFile.get("code");
            String title = (String) postFile.get("title");
            String description = (String) postFile.get("description");
            String language = (String) postFile.get("language");
            String version = (String) postFile.get("version");
            String userId = (String) postFile.get("userId");
            String token = (String) postFile.get("token");

            Snippet snippet = new Snippet();
            snippet.setSnippet(code.getBytes());
            snippet.setTitle(title);
            snippet.setDescription(description);
            snippet.setLanguage(language);
            snippet.setVersion(version);
            snippetRepository.save(snippet);

            String snippetId = snippet.getId();

            HashMap<String, String> body = new HashMap<>();
            body.put("snippetId", snippetId);
            body.put("userId", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            String rootUri = permissionsWebClient.getUriTemplateHandler().expand("/").toString();
            String url = UriComponentsBuilder.fromHttpUrl(rootUri)
                                         .path("/snippets/save/relationship")
                                         .toUriString();

            try {
                ResponseEntity<Void> response = permissionsWebClient.postForEntity(url, request, Void.class);
            } catch (HttpClientErrorException errorException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Response.withError(new Error(errorException.getStatusCode().value(), errorException.getStatusText()));
            }

            return Response.withData(snippetId);
    }

}
