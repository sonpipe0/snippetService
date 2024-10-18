package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDetails {
    private String id;
    private String description; // Agrega este campo si es necesario
    private String language; // Agrega este campo si es necesario
    private String content; // Contenido del snippet
}
