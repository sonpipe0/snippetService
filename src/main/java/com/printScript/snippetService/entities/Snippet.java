package com.printScript.snippetService.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Snippet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Lob
    @Column
    private byte[] snippet;

    @Column
    private LocalDateTime lastAccessed;

    @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL)
    private List<Test> tests;
}
