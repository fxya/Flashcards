package com.example.flashcards;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FlashcardRepository extends CrudRepository<Flashcard, Long> {
    List<Flashcard> findByDeckId(Long deckId);

    List<Flashcard> findByDeckIdOrderById(Long deckId);

    long countByDeckId(Long deckId);
}
