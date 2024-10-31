package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDTO {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String language;

    @NotBlank
    private String version;

    @NotBlank
    private String code;

    public SnippetDTO(String code, String title, String description, String language, String version) {
        this.title = title;
        this.description = description;
        this.language = language;
        this.version = version;
        this.code = code;
    }
}
