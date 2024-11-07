package com.printScript.snippetService.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.services.ConfigService;
import com.printScript.snippetService.utils.TokenUtils;

import DTO.FormatConfigDTO;

@RestController
@RequestMapping("/format")
public class FormatConfigController {

    @Autowired
    private ConfigService configService;

    @PutMapping
    public ResponseEntity<Object> putFormatConfig(@RequestBody FormatConfigDTO formatConfigDTO,
            @RequestHeader("Authorization") String token) throws IOException {
        Map<String, String> decoded = TokenUtils.decodeToken(token.substring(7));
        String userId = decoded.get("userId");
        Response<Void> response = configService.putFormatConfig(formatConfigDTO, userId, token);
        if (response.isError()) {
            return ResponseEntity.status(response.getError().code()).body(response.getError());
        }
        return ResponseEntity.ok(response.getData());
    }
}
