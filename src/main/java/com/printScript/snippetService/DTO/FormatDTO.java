package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FormatDTO {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Snippet id is required")
    private String snippetId;
}
