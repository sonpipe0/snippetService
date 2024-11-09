package com.printScript.snippetService.DTO;

import com.printScript.snippetService.entities.Snippet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SnippetCodeDetails {
    private String id;
    private String title;
    private String description;
    private String language;
    private String version;
    private String code;
    private Snippet.Status lintStatus;
}
