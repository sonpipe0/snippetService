package com.printScript.snippetService.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class LintConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private boolean restrictPrintln = false;

    @Column(nullable = false)
    private boolean restrictReadInput = false;
}
