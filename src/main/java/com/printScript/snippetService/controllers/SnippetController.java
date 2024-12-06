package com.printScript.snippetService.controllers;

import static com.printScript.snippetService.utils.Utils.checkMediaType;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.redis.ProducerInterface;
import com.printScript.snippetService.services.SnippetService;
import com.printScript.snippetService.utils.TokenUtils;

@RestController
@RequestMapping("/snippet")
public class SnippetController {

    private final SnippetService snippetService;

    @Autowired
    public SnippetController(SnippetService snippetService, ProducerInterface lintProducer) {
        this.snippetService = snippetService;
    }

    @PostMapping("/save")
    public ResponseEntity<Object> saveSnippet(@RequestBody SnippetDTO snippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, token);
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
        Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateSnippet(@RequestBody UpdateSnippetDTO updateSnippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<SnippetCodeDetails> response = snippetService.updateSnippet(updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/update/file")
    public ResponseEntity<Object> updateSnippetFile(@RequestParam MultipartFile file, @RequestParam String snippetId,
            @RequestParam String title, @RequestParam String description, @RequestParam String language,
            @RequestParam String version, @RequestHeader("Authorization") String token) {
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
        Response<SnippetCodeDetails> response = snippetService.updateSnippet(updateSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getSnippetDetails(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<SnippetCodeDetails> response = snippetService.getSnippetDetails(snippetId, token);
        System.out.println("response: " + response);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteSnippet(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<String> response = snippetService.deleteSnippet(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/share")
    public ResponseEntity<Object> shareSnippet(@RequestBody ShareSnippetDTO shareSnippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<SnippetCodeDetails> response = snippetService.shareSnippet(shareSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
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

    @GetMapping("/get/all")
    public ResponseEntity<Object> getAccessibleSnippets(@RequestHeader("Authorization") String token,
            @RequestParam(required = false) String relation, @RequestParam Integer page,
            @RequestParam Integer page_size, @RequestParam String prefix) {
        Response<PaginationAndDetails> response = snippetService.getAccessibleSnippets(token, relation, page, page_size,
                prefix);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return new ResponseEntity<>(response.getData(), HttpStatus.OK);
    }

    @GetMapping("/get/users")
    public ResponseEntity<Object> getSnippetUsers(@RequestHeader("Authorization") String token,
            @RequestParam String prefix, @RequestParam Integer page, @RequestParam Integer page_size) {
        Response<PaginatedUsers> response = snippetService.getSnippetUsers(token, prefix, page, page_size);
        String userId = TokenUtils.decodeToken(token.substring(7)).get("userId");
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatus.valueOf(response.getError().code()));
        }
        PaginatedUsers paginatedUsers = response.getData();
        List<User> filteredUsers = paginatedUsers.getUsers().stream().filter(user -> !user.getId().equals(userId))
                .toList();
        paginatedUsers.setUsers(filteredUsers);

        return new ResponseEntity<>(paginatedUsers, HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<Object> downloadSnippet(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<SnippetService.Tuple> response = snippetService.downloadSnippet(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + response.getData().name())
                .body(response.getData().code());
    }
}
