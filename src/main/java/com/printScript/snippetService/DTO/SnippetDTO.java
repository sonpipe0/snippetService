package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class SnippetDTO {

    @NonNull
    private String userId;

    @NonNull
    private String code;

    @NonNull
    private String title;

    @NonNull
    private String description;

    @NonNull
    private String language;

    @NonNull
    private String version;
}
