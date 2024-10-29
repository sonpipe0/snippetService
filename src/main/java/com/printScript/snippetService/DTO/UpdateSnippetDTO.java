package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSnippetDTO {

    private String userId;

    private String snippetId;

    private String title;

    private String description;

    private String language;

    private String version;

    private String code;

    public UpdateSnippetDTO(String code, String userId, String snippetId, String title, String description,
            String language, String version) {
        this.userId = userId;
        this.snippetId = snippetId;
        this.title = title;
        this.description = description;
        this.language = language;
        this.version = version;
        this.code = code;
    }
}
