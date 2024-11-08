package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.redis.LintProducerInterface;
import com.printScript.snippetService.services.SnippetService;

@RestController
@RequestMapping("/snippet")
public class SnippetController {

    private static final Logger logger = Logger.getLogger(SnippetController.class.getName());

    private final SnippetService snippetService;

    @Autowired
    public SnippetController(SnippetService snippetService, LintProducerInterface lintProducer) {
        this.snippetService = snippetService;
    }

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
    public ResponseEntity<Object> saveSnippetFile(@RequestParam MultipartFile file, @RequestParam String title,
            @RequestParam String description, @RequestParam String language, @RequestParam String version,
            @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        String code;
        try {
            code = new String(file.getBytes());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        SnippetDTO snippetDTO = new SnippetDTO(code, title, description, language, version);
        Response<String> response = snippetService.saveSnippet(snippetDTO, token);
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
    public ResponseEntity<Object> updateSnippetFile(@RequestParam MultipartFile file, @RequestParam String userId,
            @RequestParam String snippetId, @RequestParam String title, @RequestParam String description,
            @RequestParam String language, @RequestParam String version, @RequestHeader("Authorization") String token) {
        ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
        if (mediaTypeCheck != null) {
            return mediaTypeCheck;
        }

        String code;
        try {
            code = new String(file.getBytes());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        UpdateSnippetDTO updateSnippetDTO = new UpdateSnippetDTO(code, snippetId, title, description, language,
                version);
        Response<String> response = snippetService.updateSnippet(updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok("Snippet updated successfully");
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getSnippetDetails(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<SnippetDetails> response = snippetService.getSnippetDetails(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/share")
    public ResponseEntity<Object> shareSnippet(@RequestBody ShareSnippetDTO shareSnippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<String> response = snippetService.shareSnippet(shareSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok("Snippet shared successfully");
    }

    @GetMapping("/get/formatted")
    public ResponseEntity<Object> getFormattedSnippet(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<String> response = snippetService.getFormattedFile(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @GetMapping("/accessible")
    public ResponseEntity<Response<List<SnippetDetails>>> getAccessibleSnippets(
            @RequestHeader("Authorization") String token, @RequestParam(required = false) String relation,
            @RequestParam(required = false) String conformance) {
        Snippet.Status status = null;
        if (conformance != null) {
            status = Snippet.Status.valueOf(conformance);
        }
        Response<List<SnippetDetails>> response = snippetService.getAccessibleSnippets(token, relation, status);
        if (response.isError()) {
            return new ResponseEntity<>(response, HttpStatus.valueOf(response.getError().code()));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
