package com.printScript.snippetService.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDTO {

    private String userId;

    private String title;

    private String description;

    private String language;

    private String version;

    private String code;

    public SnippetDTO(String code, String userId, String title, String description, String language, String version) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.language = language;
        this.version = version;
        this.code = code;
    }
}
