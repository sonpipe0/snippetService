package com.printScript.snippetService.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String language;

    @Column
    private String version;

    @Lob
    @Column
    private byte[] snippet;

    @Column
    private LocalDateTime lastAccessed;

    @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL)
    private List<Test> tests;
}
