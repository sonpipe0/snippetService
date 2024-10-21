package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSnippetInfoDTO {

    @NotEmpty
    private String userId;

    @NotEmpty
    private String snippetId;

    @NotEmpty
    private String title;

    private String description;

    @NotEmpty
    private String language;

    @NotEmpty
    private String version;
}
