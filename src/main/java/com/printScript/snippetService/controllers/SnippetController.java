package com.printScript.snippetService.controllers;

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
    public ResponseEntity<Object> saveSnippet(@RequestBody Map<String, Object> body, @RequestHeader("Authorization") String token) {
        body.put("token", token);
        Response<String> response = snippetService.saveSnippet(body);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

}
