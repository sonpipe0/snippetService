package com.printScript.snippetService.DTO;

import com.printScript.snippetService.entities.Snippet;

public record SnippetDetails(String snippetId, String title, String description, String language, String version,
        String content, Snippet.Status status) {
}
