package com.example.flashcards;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DeckController {
    private final DeckService service;

    public DeckController(DeckService service) {
        this.service = service;
    }

    @GetMapping("/api/decks")
    public List<DeckSummary> getDecks() {
        return service.getDecks();
    }

    @PostMapping("/api/decks")
    public Deck createDeck(@Valid @RequestBody Deck deck) {
        return service.createDeck(deck);
    }

    @PutMapping("/api/decks/{id}")
    public Deck renameDeck(@PathVariable Long id, @Valid @RequestBody Deck deck) {
        return service.renameDeck(id, deck);
    }

    @DeleteMapping("/api/decks/{id}")
    public void deleteDeck(@PathVariable Long id) {
        service.deleteDeck(id);
    }
}
