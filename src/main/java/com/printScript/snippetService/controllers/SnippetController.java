package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.services.SnippetService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            return new ResponseEntity<>(response.getError().message(),
                    HttpStatusCode.valueOf(response.getError().code()));
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

        Response<String> response = snippetService.saveFromMultiPart(file, snippetInfoDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(),
                    HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/update/file")
    public ResponseEntity<Object> updateSnippetFile(@RequestPart MultipartFile file,
            @RequestPart UpdateSnippetDTO updateSnippetDTO, @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        Response<String> response = snippetService.updateSnippet(file, updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(),
                    HttpStatusCode.valueOf(response.getError().code()));
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
            return new ResponseEntity<>(response.getError().message(),
                    HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }
}
