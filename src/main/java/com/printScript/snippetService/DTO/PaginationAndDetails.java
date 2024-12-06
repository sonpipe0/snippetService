package com.printScript.snippetService.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationAndDetails {
    Integer page;
    Integer page_size;
    Integer count;
    private List<SnippetCodeDetails> snippetCodeDetails;
}
