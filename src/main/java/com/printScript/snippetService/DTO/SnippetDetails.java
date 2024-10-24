package com.printScript.snippetService.DTO;

import java.util.List;

import com.printScript.snippetService.errorDTO.ErrorMessage;

public record SnippetDetails(String title, String description, String language, String version, String content,
        List<ErrorMessage> lintingErrors) {
}
