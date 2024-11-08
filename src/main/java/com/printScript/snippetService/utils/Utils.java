package com.printScript.snippetService.utils;

import java.util.Set;

import org.springframework.http.*;

import com.printScript.snippetService.errorDTO.Error;

import jakarta.validation.ConstraintViolation;

public class Utils {
    public static ResponseEntity<Object> checkMediaType(String contentType) {
        if (contentType == null || (!contentType.equals("text/plain") && !contentType.equals("application/json"))) {
            return new ResponseEntity<>("Unsupported file type", HttpStatusCode.valueOf(415));
            // 415 Unsupported Media Type
        }
        return null;
    }

    public static <T> Error<?> getViolationsMessageError(Set<ConstraintViolation<T>> violations) {
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("\n");
        }
        return new Error<>(400, message.toString());
    }
}
