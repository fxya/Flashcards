package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckRepository deckRepository;

    @MockitoBean
    private FlashcardRepository flashcardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDecks_includesCardCounts() throws Exception {
        Deck deck = new Deck("General");
        when(deckRepository.findAll()).thenReturn(List.of(deck));
        when(flashcardRepository.countByDeckId(null)).thenReturn(3L);

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("General"))
                .andExpect(jsonPath("$[0].cardCount").value(3));
    }

    @Test
    void createDeck() throws Exception {
        Deck deck = new Deck("Spanish");
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spanish"));

        verify(deckRepository, times(1)).save(any(Deck.class));
    }

    @Test
    void createDeck_rejectsBlankName() throws Exception {
        Deck deck = new Deck("");

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deck)))
                .andExpect(status().isBadRequest());

        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void renameDeck_returnsNotFoundForUnknownId() throws Exception {
        when(deckRepository.findById(1L)).thenReturn(Optional.empty());
        Deck update = new Deck("New name");

        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeck_deletesItsFlashcardsFirst() throws Exception {
        when(deckRepository.existsById(1L)).thenReturn(true);
        Flashcard flashcard = new Flashcard("q", "a");
        when(flashcardRepository.findByDeckId(1L)).thenReturn(List.of(flashcard));

        mockMvc.perform(delete("/api/decks/1"))
                .andExpect(status().isOk());

        verify(flashcardRepository, times(1)).deleteAll(List.of(flashcard));
        verify(deckRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteDeck_returnsNotFoundForUnknownId() throws Exception {
        when(deckRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/decks/1"))
                .andExpect(status().isNotFound());

        verify(deckRepository, never()).deleteById(anyLong());
    }
}
