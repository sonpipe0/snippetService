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
public class FormatConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private boolean spaceBeforeColon;

    @Column(nullable = false)
    private boolean spaceAfterColon;

    @Column(nullable = false)
    private boolean spaceAroundEquals;

    @Column(nullable = false)
    private int linesBeforePrintln;

    @Column(nullable = false)
    private boolean newLineAfterSemicolon;

    @Column(nullable = false)
    private boolean enforceSpacingBetweenTokens;

    @Column(nullable = false)
    private boolean enforceSpacingAroundOperators;

    @Column(nullable = false)
    private boolean ifBraceBelowLine;

    @Column(nullable = false)
    private int indentInsideBraces;
}
