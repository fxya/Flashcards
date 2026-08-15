package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeckViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    void createDeckRedirectsToTheNewDeck() throws Exception {
        mockMvc.perform(post("/decks").param("name", "Spanish").param("order", "study"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/flashcards?deckId=*&order=study"));

        assertThat(deckRepository.findAll()).extracting(Deck::getName).contains("Spanish");
    }

    @Test
    void createDeckRejectsBlankNameViaFlashError() throws Exception {
        mockMvc.perform(post("/decks").param("name", "").param("order", "study"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/flashcards?order=study"))
                .andExpect(flash().attributeExists("deckError"));
    }

    @Test
    void deleteDeckCascadesFlashcardsAndRedirects() throws Exception {
        Deck deck = deckRepository.save(new Deck("Temporary"));
        flashcardRepository.save(new Flashcard("Q", "A", deck));

        mockMvc.perform(post("/decks/{id}/delete", deck.getId()).param("order", "study"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/flashcards?order=study"));

        assertThat(deckRepository.existsById(deck.getId())).isFalse();
    }
}
