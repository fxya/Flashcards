package com.example.flashcards;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/* This controller serves the API that the front-end uses to create, read, update,
   and delete flashcards. */

@RestController
public class FlashcardAPIController {
    private final FlashcardRepository repository;

    public FlashcardAPIController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/flashcards")
    public List<Flashcard> getFlashcards() {
        var flashcards = (List<Flashcard>) repository.findAll();
        Collections.shuffle(flashcards);
        return flashcards;
    }

    @PostMapping("/api/flashcards")
    public Flashcard createFlashcard(@RequestBody Flashcard flashcard) {
        return repository.save(flashcard);
    }

    @PutMapping("/api/flashcards/{id}")
    public Flashcard updateFlashcard(@PathVariable Long id, @RequestBody Flashcard flashcard) {
        var flashcardToUpdate = repository.findById(id).orElseThrow();
        flashcardToUpdate.setQuestion(flashcard.getQuestion());
        flashcardToUpdate.setAnswer(flashcard.getAnswer());
        return repository.save(flashcardToUpdate);
    }

    @DeleteMapping("/api/flashcards/{id}")
    public void deleteFlashcard(@PathVariable Long id) {
        repository.deleteById(id);
    }

}