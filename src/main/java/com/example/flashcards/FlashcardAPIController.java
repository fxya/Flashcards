package com.example.flashcards;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

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

}