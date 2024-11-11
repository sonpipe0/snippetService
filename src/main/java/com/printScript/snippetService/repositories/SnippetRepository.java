package com.printScript.snippetService.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.printScript.snippetService.entities.Snippet;

public interface SnippetRepository extends JpaRepository<Snippet, String> {

    List<Snippet> findByIdInAndTitleStartingWith(List<String> ids, String prefix, Pageable pageable);
}
