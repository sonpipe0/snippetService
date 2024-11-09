package com.printScript.snippetService.DTO;

import com.printScript.snippetService.entities.Snippet;

public record SnippetDetails(String id, String title, String description, String language, String version,
        Snippet.Status lintStatus) {
}
