package com.example.flashcards;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class FlashcardService {

    private static final List<ReviewStatus> STUDY_PRIORITY =
            List.of(ReviewStatus.UNKNOWN, ReviewStatus.UNSEEN, ReviewStatus.KNOWN);

    private final FlashcardRepository repository;
    private final DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository repository, DeckRepository deckRepository) {
        this.repository = repository;
        this.deckRepository = deckRepository;
    }

    public List<Flashcard> getFlashcards(Long deckId, String order) {
        return switch (order) {
            case "shuffle" -> shuffled(repository.findByDeckId(deckId));
            case "study" -> orderForStudy(repository.findByDeckId(deckId));
            default -> repository.findByDeckIdOrderById(deckId);
        };
    }

    private List<Flashcard> shuffled(List<Flashcard> flashcards) {
        Collections.shuffle(flashcards);
        return flashcards;
    }

    /**
     * Cards the learner got wrong resurface first, then cards never reviewed, with
     * cards already marked known deprioritized to the end. Each group is shuffled
     * internally so repeat study sessions don't always show the same sequence.
     */
    private List<Flashcard> orderForStudy(List<Flashcard> flashcards) {
        Map<ReviewStatus, List<Flashcard>> byStatus = new EnumMap<>(ReviewStatus.class);
        for (ReviewStatus status : ReviewStatus.values()) {
            byStatus.put(status, new ArrayList<>());
        }
        for (Flashcard flashcard : flashcards) {
            byStatus.get(flashcard.getStatus()).add(flashcard);
        }

        List<Flashcard> ordered = new ArrayList<>(flashcards.size());
        for (ReviewStatus status : STUDY_PRIORITY) {
            List<Flashcard> group = byStatus.get(status);
            Collections.shuffle(group);
            ordered.addAll(group);
        }
        return ordered;
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

    public Flashcard reviewFlashcard(Long id, ReviewStatus status) {
        var flashcard = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No flashcard found with id " + id));
        flashcard.setStatus(status);
        flashcard.setLastReviewedAt(Instant.now());
        return repository.save(flashcard);
    }
}
