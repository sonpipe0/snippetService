package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;
import com.printScript.snippetService.DTO.PostFile;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.DTO.UpdateSnippetDTO;
import com.printScript.snippetService.services.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/save/file")
    public ResponseEntity<Object> saveSnippetFile(
            @RequestParam MultipartFile file,
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String language,
            @RequestHeader("Authorization") String token) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("text/plain") && !contentType.equals("application/json"))) {
            return new ResponseEntity<>("Unsupported file type", HttpStatusCode.valueOf(415)); // 415 Unsupported Media Type
        }
        Map<String,Object> body = Map.of("file", file, "userId", userId, "token", token, "title", title, "description", description, "language", language);
        Response<String> response = snippetService.saveFromMultiPart(body);
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

    @PostMapping("/update/file")
    public ResponseEntity<Object> updateSnippetFile(
            @RequestPart MultipartFile file,
            @RequestPart UpdateSnippetDTO updateSnippetDTO,
            @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        Response<String> response = snippetService.updateSnippet(file, updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok("Snippet updated successfully");
    }

}
