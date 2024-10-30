package com.printScript.snippetService.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDTO {

    private String title;

    private String description;

    private String language;

    private String version;

    private String code;

    public SnippetDTO(String code, String title, String description, String language, String version) {
        this.title = title;
        this.description = description;
        this.language = language;
        this.version = version;
        this.code = code;
    }
}
