package com.example.testgraphql.repository;

import com.example.testgraphql.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByTitle(String title);

    List<Note> findByRecipeIdIn(Collection<Long> recipeIds);

    List<Note> findByRecipeId(long recipeId);

    @Query("SELECT n.recipeId AS recipeId, COUNT(n.id) AS notesCount FROM Note AS n WHERE n.recipeId IN :recipeIds GROUP BY n.recipeId")
    List<RecipeToNotesCount> countNotesByRecipeId(Collection<Long> recipeIds);

    interface RecipeToNotesCount {
        long getRecipeId();

        long getNotesCount();
    }
}
