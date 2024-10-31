package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LintDTO {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Version is required")
    private String version;
}
