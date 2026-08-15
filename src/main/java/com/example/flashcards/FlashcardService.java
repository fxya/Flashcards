package com.example.flashcards;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FlashcardService {

    private final FlashcardRepository repository;

    public FlashcardService(FlashcardRepository repository) {
        this.repository = repository;
    }

    public List<Flashcard> getFlashcards(boolean shuffle) {
        var flashcards = (List<Flashcard>) repository.findAll();
        if (shuffle) {
            Collections.shuffle(flashcards);
        }
        return flashcards;
    }

    public Flashcard createFlashcard(Flashcard flashcard) {
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
