package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShareSnippetDTO {

    @NotBlank
    private String snippetId;

    @NotBlank
    private String username;
}
