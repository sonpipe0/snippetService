package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSnippetDTO {

    @NotBlank(message = "Snippet id is required")
    private String snippetId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Code is required")
    private String code;

    public UpdateSnippetDTO(String code, String snippetId, String title, String description, String language,
            String version) {
        this.snippetId = snippetId;
        this.title = title;
        this.description = description;
        this.language = language;
        this.version = version;
        this.code = code;
    }
}
