package com.printScript.snippetService.DTO;

import com.printScript.snippetService.errorDTO.SnippetError;

public record IdResponse(String id, SnippetError error) {
}
