package com.printScript.snippetService.controllers;

import com.printScript.snippetService.DTO.PostFile;
import com.printScript.snippetService.DTO.PostTest;
import com.printScript.snippetService.DTO.SnippetResponse;
import com.printScript.snippetService.errorDTO.SnippetError;
import com.printScript.snippetService.services.SnippetStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller("snippetStorage")
public class SnippetStorageController {

    @Autowired
    SnippetStorageService storageService;

    @PostMapping("snippet/save")
    public ResponseEntity<Object> saveSnippet(@RequestBody PostFile postFile) {
        //check permissions
        SnippetError error = storageService.saveSnippet(postFile.file());
        if (error != null) {
            return new ResponseEntity<>(error.message(), HttpStatusCode.valueOf(error.code()));
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("test/save")
    public ResponseEntity<Object> saveTest(@RequestBody PostTest test)  {
        SnippetError error = storageService.saveTest(test.snippetId(), test.expectedOutput());
        if (error != null) {
            return new ResponseEntity<>(error.message(), HttpStatusCode.valueOf(error.code()));
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
