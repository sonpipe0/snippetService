package com.printScript.snippetService.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaginatedUsers {
    Integer page;
    Integer page_size;
    Integer count;
    List<User> users;
}
