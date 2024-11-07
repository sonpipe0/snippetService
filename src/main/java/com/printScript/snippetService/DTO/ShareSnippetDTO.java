package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShareSnippetDTO {

    @NotBlank(message = "Snippet ID is required")
    private String snippetId;

    @NotBlank(message = "Username is required")
    private String username;
}
