package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Test
    void studyOrderPrioritizesUnknownThenUnseenThenKnown() {
        FlashcardService service = new FlashcardService(flashcardRepository, deckRepository);

        Flashcard known = new Flashcard("known-q", "known-a");
        known.setStatus(ReviewStatus.KNOWN);
        Flashcard unseen = new Flashcard("unseen-q", "unseen-a");
        Flashcard unknown = new Flashcard("unknown-q", "unknown-a");
        unknown.setStatus(ReviewStatus.UNKNOWN);

        when(flashcardRepository.findByDeckId(1L))
                .thenReturn(new ArrayList<>(List.of(known, unseen, unknown)));

        List<Flashcard> ordered = service.getFlashcards(1L, "study");

        assertThat(ordered).hasSize(3);
        assertThat(ordered.get(0).getStatus()).isEqualTo(ReviewStatus.UNKNOWN);
        assertThat(ordered.get(1).getStatus()).isEqualTo(ReviewStatus.UNSEEN);
        assertThat(ordered.get(2).getStatus()).isEqualTo(ReviewStatus.KNOWN);
    }

    @Test
    void naturalOrderIsSortedById() {
        FlashcardService service = new FlashcardService(flashcardRepository, deckRepository);

        Flashcard a = new Flashcard("a", "a");
        Flashcard b = new Flashcard("b", "b");
        when(flashcardRepository.findByDeckIdOrderById(1L)).thenReturn(new ArrayList<>(List.of(a, b)));

        List<Flashcard> ordered = service.getFlashcards(1L, "natural");

        assertThat(ordered).containsExactly(a, b);
    }
}
