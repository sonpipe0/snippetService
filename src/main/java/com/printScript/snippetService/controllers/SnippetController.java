package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.services.SnippetService;

@RestController
@RequestMapping("/snippet")
public class SnippetController {

    @Autowired
    SnippetService snippetService;

    @PostMapping("/save")
    public ResponseEntity<Object> saveSnippet(@RequestBody SnippetDTO snippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<String> response = snippetService.saveSnippet(snippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/save/file")
    public ResponseEntity<Object> saveSnippetFile(@RequestParam MultipartFile file,
            @RequestParam SnippetInfoDTO snippetInfoDTO, @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        Response<String> response = snippetService.saveSnippetFile(file, snippetInfoDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateSnippet(@RequestBody UpdateSnippetDTO updateSnippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<String> response = snippetService.updateSnippet(updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok("Snippet updated successfully");
    }

    @PostMapping("/update/file")
    public ResponseEntity<Object> updateSnippetFile(@RequestPart MultipartFile file,
            @RequestPart UpdateSnippetInfoDTO updateSnippetInfoDTO, @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        Response<String> response = snippetService.updateSnippetFile(file, updateSnippetInfoDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok("Snippet updated successfully");
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getSnippetDetails(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        String snippetId = body.get("snippetId");
        String userId = body.get("userId");

        Response<SnippetDetails> response = snippetService.getSnippetDetails(snippetId, userId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @GetMapping("/accessible")
    public ResponseEntity<Response<List<SnippetDetails>>> getAccessibleSnippets(
            @RequestParam String userId,
            @RequestParam String token,
            @RequestParam String relation,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) String languageFilter,
            @RequestParam(required = false) Boolean isValid,
            @RequestParam(required = false) String sortBy) {
        Response<List<SnippetDetails>> response = snippetService.getAccessibleSnippets(userId, token, relation, nameFilter, languageFilter, isValid, sortBy);
        if (response.isError()) {
            return new ResponseEntity<>(response, HttpStatus.valueOf(response.getError().code()));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
