package com.printScript.snippetService.controllers;

import com.printScript.snippetService.DTO.PostTest;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testService;

    @PostMapping("/save")
    public ResponseEntity<Object> saveTest(@RequestBody PostTest test) {
        Map<String, Object> body = Map.of("snippetId", test.snippetId(), "expectedOutput", test.expectedOutput());
        Response<String> response = testService.saveTest(body);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteTest(@RequestBody Map<String, String> body) {
        Response<String> response = testService.deleteTest(body.get("testId"));
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getTest(@RequestBody Map<String, String> body) {
        Response<List<String>> response = testService.getTestSuites(body.get("snippetId"));
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().message(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }
}
