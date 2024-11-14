package com.printScript.snippetService.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.DTO.TestDTO;
import com.printScript.snippetService.services.TestService;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createTest(@RequestBody TestDTO testDTO,
            @RequestHeader("Authorization") String token) {
        Response<String> response = testService.createTest(testDTO, token);
        System.out.println(token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @GetMapping("/get-all")
    public ResponseEntity<Object> getTestsForSnippet(@RequestParam String snippetId,
            @RequestHeader("Authorization") String token) {
        Response<List<TestDTO>> response = testService.getTestsForSnippet(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateTest(@RequestBody TestDTO testDTO,
            @RequestHeader("Authorization") String token) {
        Response<Void> response = testService.updateTest(testDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteTest(@RequestParam String testId,
            @RequestHeader("Authorization") String token) {
        Response<Void> response = testService.deleteTest(testId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/run")
    public ResponseEntity<Object> runTest(@RequestBody TestDTO testDTO, @RequestHeader("Authorization") String token) {
        Response<Void> response = testService.runTest(testDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok().build();
    }
}
