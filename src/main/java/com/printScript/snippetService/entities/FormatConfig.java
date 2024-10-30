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
    private String id;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String version;
}
