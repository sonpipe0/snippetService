package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class SnippetDTO {

    private String userId;

    private String code;

    @NonNull
    private String title;

    private String description;

    @NonNull
    private String language;

    @NonNull
    private String version;
}
