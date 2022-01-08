package com.example.testgraphql;

import com.example.testgraphql.model.Note;
import com.example.testgraphql.model.Recipe;
import com.example.testgraphql.repository.NoteRepository;
import com.example.testgraphql.repository.NoteRepository.RecipeToNotesCount;
import com.example.testgraphql.repository.RecipeRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Controller
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final NoteRepository noteRepository;

    @Autowired
    public RecipeController(RecipeRepository recipeRepository,
                            NoteRepository noteRepository) {
        this.recipeRepository = recipeRepository;
        this.noteRepository = noteRepository;
    }


    @MutationMapping
    @Transactional
    public RecipeDto createRecipe(@Argument CreateRecipeInput input) {
        if (recipeRepository.findByTitle(input.title).isPresent()) {
            throw new RuntimeException("Title is already taken");
        }
        final var recipe = recipeRepository.save(new Recipe()
            .title(input.title)
            .description(input.description));

        final var notes = input.notes.stream()
            .map(n -> new Note().recipeId(recipe.id()).description(n).title(n))
            .collect(toUnmodifiableList());
        noteRepository.saveAll(notes);

        return new RecipeDto(recipe.id(), recipe.title(), recipe.description());
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<RecipeDto> getRecipes() {
        return recipeRepository.findAll().stream()
            .map(r -> new RecipeDto(r.id(), r.title(), r.description()))
            .collect(toUnmodifiableList());
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public RecipeDto getRecipe(@Argument("id") long id) {
        return recipeRepository.findById(id)
            .map(r -> new RecipeDto(r.id(), r.title(), r.description()))
            .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    @Transactional(readOnly = true)
    @BatchMapping(typeName = "Recipe", value = "notesCount")
    public Mono<Map<RecipeDto, Long>> getNotesCount(List<RecipeDto> recipes) {
        return Mono.fromSupplier(() -> {
            final var recipeIdToRecipe = recipes.stream().collect(toUnmodifiableMap(r -> r.id, r -> r));
            final var recipeIdToNotesCount = noteRepository.countNotesByRecipeId(recipeIdToRecipe.keySet())
                .stream()
                .collect(toUnmodifiableMap(RecipeToNotesCount::getRecipeId, RecipeToNotesCount::getNotesCount));

            return recipes
                .stream()
                .collect(toMap(identity(), recipe -> recipeIdToNotesCount.getOrDefault(recipe.id, 0L)));
        });
    }

    @Transactional(readOnly = true)
    @BatchMapping(typeName = "Recipe")
    public Mono<Map<RecipeDto, List<NoteDto>>> notes(List<RecipeDto> recipes) {
        return Mono.fromSupplier(() -> {
            final var recipeIdToRecipe = recipes.stream().collect(toUnmodifiableMap(r -> r.id, r -> r));
            final var recipeIdToNotes = noteRepository.findByRecipeIdIn(recipeIdToRecipe.keySet())
                .stream()
                .collect(groupingBy(
                    Note::recipeId,
                    mapping(note -> new NoteDto(note.id(), note.title(), note.description()), toUnmodifiableList())
                ));

            return recipes
                .stream()
                .collect(toUnmodifiableMap(identity(), recipe -> recipeIdToNotes.getOrDefault(recipe.id, emptyList())));
        });
    }

    @QueryMapping
    public List<NoteDto> getNotes(@Argument("recipeId") long recipeId) {
        return noteRepository.findByRecipeId(recipeId).stream()
            .map(n -> new NoteDto(n.id(), n.title(), n.description()))
            .collect(toUnmodifiableList());
    }

    @Data
    public static final class CreateRecipeInput {
        private final String title;
        private final String description;
        private final List<String> notes;
    }

    @Data
    public static final class RecipeDto {
        private final long id;
        private final String title;
        private final String description;
    }

    @Data
    public static final class NoteDto {
        private final long id;
        private final String title;
        private final String description;
    }
}
