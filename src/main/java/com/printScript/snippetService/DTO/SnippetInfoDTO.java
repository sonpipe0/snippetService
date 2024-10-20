package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnippetInfoDTO {

    private String userId;

    private String title;

    private String description;

    private String language;

    private String version;
}
