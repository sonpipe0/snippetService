package com.printScript.snippetService.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printScript.snippetService.entities.Test;

public interface TestRepository extends JpaRepository<Test, String> {
    List<Test> findBySnippetId(String snippetId);
}
