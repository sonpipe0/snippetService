package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSnippetDTO {

    @NotBlank
    private String snippetId;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String language;

    @NotBlank
    private String version;

    @NotBlank
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
