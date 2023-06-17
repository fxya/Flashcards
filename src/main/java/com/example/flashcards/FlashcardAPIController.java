package com.example.flashcards;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FlashcardAPIController {
    private final FlashcardRepository repository;

    public FlashcardAPIController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/flashcards")
    public List<Flashcard> getFlashcards() {
        return (List<Flashcard>) repository.findAll();
    }
}