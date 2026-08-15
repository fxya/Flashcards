package com.example.flashcards;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public DeckService(DeckRepository deckRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public List<DeckSummary> getDecks() {
        List<DeckSummary> summaries = new ArrayList<>();
        for (Deck deck : deckRepository.findAll()) {
            summaries.add(new DeckSummary(deck.getId(), deck.getName(), flashcardRepository.countByDeckId(deck.getId())));
        }
        return summaries;
    }

    public Deck createDeck(Deck deck) {
        return deckRepository.save(deck);
    }

    public Deck renameDeck(Long id, Deck update) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No deck found with id " + id));
        deck.setName(update.getName());
        return deckRepository.save(deck);
    }

    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            throw new NoSuchElementException("No deck found with id " + id);
        }
        flashcardRepository.deleteAll(flashcardRepository.findByDeckId(id));
        deckRepository.deleteById(id);
    }
}
