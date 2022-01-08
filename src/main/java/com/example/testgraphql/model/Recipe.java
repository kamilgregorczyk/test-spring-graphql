package com.example.testgraphql.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Getter
@Accessors(fluent = true, chain = true)
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;

    @OneToMany(cascade = ALL, mappedBy = "recipe", fetch = LAZY)
    @ToString.Exclude
    private Set<Note> notes = new HashSet<>();


    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "recipe_to_category",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id"))
    @ToString.Exclude
    private Set<Category> categories = new HashSet<>();

}
