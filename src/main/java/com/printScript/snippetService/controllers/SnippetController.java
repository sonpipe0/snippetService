package com.printScript.snippetService.controllers;

import com.printScript.snippetService.DTO.PostFile;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.services.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/snippet")
public class SnippetController {

    @Autowired
    SnippetService snippetService;

    @PostMapping("/save")
    public ResponseEntity<Object> saveSnippet(@RequestBody PostFile postFile, @RequestHeader("Authorization") String token) {
        Map<String,Object> body = new HashMap<>();
        body.put("file", postFile.file());
        body.put("userId", postFile.userId());
        body.put("token", token);
        Response<String> response = snippetService.saveSnippet(body);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteSnippet(@RequestBody Map<String, String> body) {
        Response<String> response = snippetService.deleteSnippet(body.get("snippetId"));
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getSnippet(@RequestBody Map<String, String> body) {
        Response<String> response = snippetService.getSnippet(body.get("snippetId"));
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }
}
