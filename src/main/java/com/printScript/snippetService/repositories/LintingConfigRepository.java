package com.printScript.snippetService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printScript.snippetService.entities.LintConfig;

public interface LintingConfigRepository extends JpaRepository<LintConfig, String> {
}
