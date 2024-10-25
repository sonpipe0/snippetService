package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
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

    @PostMapping("/redis")
    public ResponseEntity<Object> postToV1StreamCiclon() {
        logger.info("Received request to post to v1 stream ciclon");
        try {
            snippetService.postToCyclon();
            return ResponseEntity.ok("Event produced successfully");
        } catch (Exception e) {
            logger.severe("Error while posting to v1 stream ciclon: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
