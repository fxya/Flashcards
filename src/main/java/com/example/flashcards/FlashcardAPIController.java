package com.example.flashcards;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* This controller serves the API that the front-end uses to create, read, update,
   and delete flashcards. */

@RestController
public class FlashcardAPIController {
    private final FlashcardService service;

    public FlashcardAPIController(FlashcardService service) {
        this.service = service;
    }

    @GetMapping("/api/decks/{deckId}/flashcards")
    public List<Flashcard> getFlashcards(@PathVariable Long deckId,
                                          @RequestParam(defaultValue = "false") boolean shuffle) {
        return service.getFlashcards(deckId, shuffle);
    }

    @PostMapping("/api/decks/{deckId}/flashcards")
    public Flashcard createFlashcard(@PathVariable Long deckId, @Valid @RequestBody Flashcard flashcard) {
        return service.createFlashcard(deckId, flashcard);
    }

    @PutMapping("/api/flashcards/{id}")
    public Flashcard updateFlashcard(@PathVariable Long id, @Valid @RequestBody Flashcard flashcard) {
        return service.updateFlashcard(id, flashcard);
    }

    @DeleteMapping("/api/flashcards/{id}")
    public void deleteFlashcard(@PathVariable Long id) {
        service.deleteFlashcard(id);
    }

}
