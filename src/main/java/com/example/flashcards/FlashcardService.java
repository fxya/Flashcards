package com.example.flashcards;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FlashcardService {

    private final FlashcardRepository repository;
    private final DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository repository, DeckRepository deckRepository) {
        this.repository = repository;
        this.deckRepository = deckRepository;
    }

    public List<Flashcard> getFlashcards(Long deckId, boolean shuffle) {
        var flashcards = repository.findByDeckId(deckId);
        if (shuffle) {
            Collections.shuffle(flashcards);
        }
        return flashcards;
    }

    public Flashcard createFlashcard(Long deckId, Flashcard flashcard) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new NoSuchElementException("No deck found with id " + deckId));
        flashcard.setDeck(deck);
        return repository.save(flashcard);
    }

    public Flashcard updateFlashcard(Long id, Flashcard flashcard) {
        var flashcardToUpdate = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No flashcard found with id " + id));
        flashcardToUpdate.setQuestion(flashcard.getQuestion());
        flashcardToUpdate.setAnswer(flashcard.getAnswer());
        return repository.save(flashcardToUpdate);
    }

    public void deleteFlashcard(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("No flashcard found with id " + id);
        }
        repository.deleteById(id);
    }
}
