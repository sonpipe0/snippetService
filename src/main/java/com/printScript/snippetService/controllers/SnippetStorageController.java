package com.printScript.snippetService.controllers;

import com.printScript.snippetService.DTO.IdResponse;
import com.printScript.snippetService.DTO.PostFile;
import com.printScript.snippetService.DTO.PostTest;
import com.printScript.snippetService.DTO.SnippetResponse;
import com.printScript.snippetService.errorDTO.SnippetError;
import com.printScript.snippetService.response.HasPassed;
import com.printScript.snippetService.services.SnippetStorageService;
import com.printScript.snippetService.services.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SnippetStorageController {

    @Autowired
    SnippetStorageService storageService;

    @Autowired
    WebClientService printScriptWebClient;
    @Autowired
    WebClientService permissionsWebClient;

    @PostMapping("snippet/save")
    public ResponseEntity<Object> saveSnippet(@RequestBody PostFile postFile) {
        IdResponse response = storageService.saveSnippet(postFile.file());
        if (response.error() != null) {
            return new ResponseEntity<>(response.error().message(), HttpStatusCode.valueOf(response.error().code()));
        }
        HashMap<String, String> body = new HashMap<>();
        body.put("snippetId", response.id());
        body.put("userId", postFile.userId());
        Mono<HasPassed> serverResponse = permissionsWebClient.post("/snippet/load/relationship", body, HasPassed.class );
        HasPassed hasPassed = serverResponse.block();
        assert hasPassed != null;
        if (hasPassed.hasPassed()) {
            return ResponseEntity.ok(response.id());
        } else {
            storageService.deleteSnippet(response.id());
            return new ResponseEntity<>("Permission denied", HttpStatusCode.valueOf(403));
        }
    }

    @PostMapping("test/save")
    public ResponseEntity<Object> saveTest(@RequestBody PostTest test)  {
        IdResponse response = storageService.saveTest(test.snippetId(), test.expectedOutput());
        if ( response.error() != null) {
            return new ResponseEntity<>(response.error().message(), HttpStatusCode.valueOf(response.error().code()));
        }
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("snippet/delete")
    public ResponseEntity<Object> deleteSnippet(@RequestBody Map<String,String> body) {
        SnippetError error = storageService.deleteSnippet(body.get("snippetId"));
        if (error != null) {
            return new ResponseEntity<>(error.message(), HttpStatusCode.valueOf(error.code()));
        }
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("test/delete")
    public ResponseEntity<Object> deleteTest(@RequestBody Map<String,String> body) {
        SnippetError error = storageService.deleteTest(body.get("testId"));
        if (error != null) {
            return new ResponseEntity<>(error.message(), HttpStatusCode.valueOf(error.code()));
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("snippet/get")
    public ResponseEntity<Object> getSnippet(@RequestBody Map<String,String> body) {
        SnippetResponse response = storageService.getSnippet(body.get("snippetId"));
        if (response.hasError()) {
            return new ResponseEntity<>(response.error().message(), HttpStatusCode.valueOf(response.error().code()));
        }
        return ResponseEntity.ok(response.response());
    }

    @GetMapping("test/get")
    public ResponseEntity<Object> getTest(@RequestBody Map<String,String> body) {
        List<String> response = storageService.getTestSuites(body.get("snippetId"));
        return ResponseEntity.ok(response);
    }
}
