package com.printScript.snippetService.DTO;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Extension is required")
    private String extension;

    @NotBlank(message = "Code is required")
    private String code;

    public SnippetDTO(String code, String title, String description, String language, String extension) {
        this.title = title;
        this.description = description;
        this.language = language;
        this.extension = extension;
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SnippetDTO snippetDTO = (SnippetDTO) obj;
        return Objects.equals(title, snippetDTO.title) && Objects.equals(description, snippetDTO.description)
                && Objects.equals(language, snippetDTO.language) && Objects.equals(extension, snippetDTO.extension)
                && Objects.equals(code, snippetDTO.code);
    }
}
