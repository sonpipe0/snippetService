package com.printScript.snippetService.DTO;

import com.printScript.snippetService.entities.LintStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllSnippetDetailsDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String language;

    @NotBlank
    private String version;

    @NotBlank
    private String content;

    @NotNull
    private LintStatus lintStatus;;
}
