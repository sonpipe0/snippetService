package com.printScript.snippetService.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Lob
    @Column
    private byte[] expectedOutput;

    @ManyToOne
    @JoinColumn(name = "snippet_id")
    private Snippet snippet;
}
