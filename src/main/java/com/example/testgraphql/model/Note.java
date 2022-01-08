package com.example.testgraphql.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Accessors(fluent = true, chain = true)
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;
    private String title;
    private String description;

    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    @ManyToOne(targetEntity = Recipe.class, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Recipe recipe;

    @Column(name = "recipe_id")
    private long recipeId;

}
