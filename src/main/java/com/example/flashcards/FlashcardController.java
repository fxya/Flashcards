package com.example.flashcards;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class FlashcardController {
    private final FlashcardRepository repository;

    FlashcardController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/flashcards")
    public List<Flashcard> getFlashcards() {
        return (List<Flashcard>) repository.findAll();
    }

    // POST
    // PUT
    // DELETE INDIVIDUAL
}
