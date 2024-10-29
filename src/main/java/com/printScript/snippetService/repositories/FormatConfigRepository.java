package com.printScript.snippetService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printScript.snippetService.entities.FormatConfig;

public interface FormatConfigRepository extends JpaRepository<FormatConfig, String> {
}
