package com.printScript.snippetService.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.printScript.snippetService.entities.Test;

public interface TestRepository extends JpaRepository<Test, String> {

    @Query("SELECT t.expectedOutput FROM Test t WHERE t.snippet.id = :snippetId")
    List<String> findAllExpectedOutputBySnippetId(@Param("snippetId") String snippetId);
}
