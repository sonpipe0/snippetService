package com.printScript.snippetService.entities;

import java.util.ArrayList;
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

    @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL)
    private List<Test> tests;

    public List<String> getInvalidFields() {
        List<String> invalidFields = new ArrayList<>();
        if (title == null || title.isEmpty()) {
            invalidFields.add("title");
        }
        if (language == null || language.isEmpty()) {
            invalidFields.add("language");
        }
        if (version == null || version.isEmpty()) {
            invalidFields.add("version");
        }
        return invalidFields;
    }

    public Object getUserId() {
        return this.getUserId();
    }
}
