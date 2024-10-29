package com.printScript.snippetService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printScript.snippetService.entities.Snippet;

public interface SnippetRepository extends JpaRepository<Snippet, String> {
}
