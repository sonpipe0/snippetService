package com.printScript.snippetService.repositories;

import com.printScript.snippetService.entities.Snippet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, String> {
}
