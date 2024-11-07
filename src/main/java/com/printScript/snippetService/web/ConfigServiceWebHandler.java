package com.printScript.snippetService.web;

import static com.printScript.snippetService.utils.Utils.getRequest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.services.RestTemplateService;

@Component
public class ConfigServiceWebHandler {

    private final RestTemplate permissionsWebClient;
    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConfigServiceWebHandler(RestTemplateService permissionsRestTemplate,
            RestTemplateService printScriptRestTemplate, ObjectMapper objectMapper) {
        this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    public Response<List<String>> getAllSnippets(String userId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            String response = getRequest(permissionsWebClient, "/snippets/get/all/edit", request, String.class,
                    Map.of());
            List<String> snippetIds = objectMapper.readValue(response, new TypeReference<>() {
            });
            return Response.withData(snippetIds);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        } catch (JsonProcessingException e) {
            return Response.withError(new Error<>(400, "Failed to get snippets"));
        }
    }
}
