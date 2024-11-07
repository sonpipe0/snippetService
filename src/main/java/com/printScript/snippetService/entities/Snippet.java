package com.printScript.snippetService.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Snippet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status lintStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status formatStatus;

    @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL)
    private List<Test> tests;

    public enum Status {
        IN_PROGRESS, COMPLIANT, NON_COMPLIANT
    }
}
