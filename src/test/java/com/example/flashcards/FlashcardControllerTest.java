package com.example.flashcards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/* Real-DB integration tests (rolled back per test via @Transactional) rather than
   mocked repositories: the controller reconstructs an ordered flashcard queue by id
   on every request, which is easier and more robust to verify against a real
   database than by hand-stubbing every repository call. */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    private Deck deck;
    private Flashcard card1;
    private Flashcard card2;
    private String queue;

    @BeforeEach
    void setUp() {
        deck = deckRepository.save(new Deck("Test Deck"));
        card1 = flashcardRepository.save(new Flashcard("Q1", "A1", deck));
        card2 = flashcardRepository.save(new Flashcard("Q2", "A2", deck));
        queue = card1.getId() + "," + card2.getId();
    }

    @Test
    void indexRendersDeckAndFirstCard() throws Exception {
        mockMvc.perform(get("/flashcards").param("deckId", deck.getId().toString()).param("order", "natural"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/html;charset=UTF-8")))
                .andExpect(content().string(containsString("Test Deck")))
                .andExpect(content().string(containsString("Q1")));
    }

    @Test
    void indexWithNoDecksShowsEmptyState() throws Exception {
        flashcardRepository.deleteAll();
        deckRepository.deleteAll();

        mockMvc.perform(get("/flashcards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No decks yet")));
    }

    @Test
    void nextAdvancesWithinTheGivenQueue() throws Exception {
        mockMvc.perform(get("/flashcards/study/next")
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Q2")));
    }

    @Test
    void previousDoesNotGoBelowZero() throws Exception {
        mockMvc.perform(get("/flashcards/study/previous")
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Q1")));
    }

    @Test
    void modeToggleShowsAddForm() throws Exception {
        mockMvc.perform(get("/flashcards/study/mode")
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("mode", "add"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"question\"")))
                .andExpect(content().string(containsString("name=\"answer\"")));
    }

    @Test
    void createFlashcardAddsCardAndSelectsIt() throws Exception {
        mockMvc.perform(post("/flashcards/study/create")
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("question", "Q3")
                        .param("answer", "A3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Q3")));

        assertThat(flashcardRepository.countByDeckId(deck.getId())).isEqualTo(3);
    }

    @Test
    void createFlashcardRejectsBlankFieldsWithoutSaving() throws Exception {
        mockMvc.perform(post("/flashcards/study/create")
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("question", "")
                        .param("answer", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("must not be blank")));

        assertThat(flashcardRepository.countByDeckId(deck.getId())).isEqualTo(2);
    }

    @Test
    void editFlashcardUpdatesContent() throws Exception {
        mockMvc.perform(post("/flashcards/{id}/edit", card1.getId())
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("question", "Updated Q1")
                        .param("answer", "Updated A1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Updated Q1")));

        assertThat(flashcardRepository.findById(card1.getId()).orElseThrow().getQuestion())
                .isEqualTo("Updated Q1");
    }

    @Test
    void deleteFlashcardRemovesItAndShowsNextCard() throws Exception {
        mockMvc.perform(delete("/flashcards/{id}", card1.getId())
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Q2")));

        assertThat(flashcardRepository.existsById(card1.getId())).isFalse();
    }

    @Test
    void reviewMarksStatusAndAdvancesToNextCard() throws Exception {
        mockMvc.perform(post("/flashcards/{id}/review", card1.getId())
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("status", "KNOWN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Q2")))
                .andExpect(content().string(containsString("1 known")));

        assertThat(flashcardRepository.findById(card1.getId()).orElseThrow().getStatus())
                .isEqualTo(ReviewStatus.KNOWN);
    }

    @Test
    void reviewingUnknownFlashcardReturnsNotFound() throws Exception {
        mockMvc.perform(post("/flashcards/{id}/review", 999999L)
                        .param("deckId", deck.getId().toString())
                        .param("order", "natural")
                        .param("queue", queue)
                        .param("index", "0")
                        .param("status", "KNOWN"))
                .andExpect(status().isNotFound());
    }
}
