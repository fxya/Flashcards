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
                                          @RequestParam(defaultValue = "natural") String order) {
        return service.getFlashcards(deckId, order);
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

    @PostMapping("/api/flashcards/{id}/review")
    public Flashcard reviewFlashcard(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return service.reviewFlashcard(id, request.status());
    }

}
