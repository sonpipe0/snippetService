package com.printScript.snippetService.DTO;

import com.printScript.snippetService.errorDTO.SnippetError;


public record SnippetResponse( String response, SnippetError error) {
    public boolean hasError() {
        return error != null;
    }
}
