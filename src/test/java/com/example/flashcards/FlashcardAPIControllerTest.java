package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FlashcardAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlashcardRepository repository;

    @MockitoBean
    private DeckRepository deckRepository;

    @Autowired
    private ObjectMapper objectMapper; //for object to JSON conversion

    @Test
    void getFlashcards() throws Exception {
        when(repository.findByDeckId(1L)).thenReturn(new ArrayList<>(List.of(
                new Flashcard("question1", "answer1"), new Flashcard("question2", "answer2"))));

        mockMvc.perform(get("/api/decks/1/flashcards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2));

        verify(repository, times(1)).findByDeckId(1L);
    }

    @Test
    void createFlashcard() throws Exception {
        when(deckRepository.findById(1L)).thenReturn(Optional.of(new Deck("General")));
        Flashcard flashcard = new Flashcard("question", "answer");
        when(repository.save(any(Flashcard.class))).thenReturn(flashcard);

        mockMvc.perform(post("/api/decks/1/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard)))
                .andExpect(status().isOk());

        verify(repository, times(1)).save(any(Flashcard.class));
    }

    @Test
    void createFlashcard_returnsNotFoundForUnknownDeck() throws Exception {
        when(deckRepository.findById(1L)).thenReturn(Optional.empty());
        Flashcard flashcard = new Flashcard("question", "answer");

        mockMvc.perform(post("/api/decks/1/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard)))
                .andExpect(status().isNotFound());

        verify(repository, never()).save(any(Flashcard.class));
    }

    @Test
    void updateFlashcard() throws Exception {
        Flashcard existingFlashcard = new Flashcard("oldQuestion", "oldAnswer");
        when(repository.findById(1L)).thenReturn(Optional.of(existingFlashcard));
        when(repository.save(any(Flashcard.class))).thenReturn(existingFlashcard);

        Flashcard newFlashcard = new Flashcard("newQuestion", "newAnswer");
        mockMvc.perform(put("/api/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFlashcard)))
                .andExpect(status().isOk());

        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Flashcard.class));
    }

    @Test
    void deleteFlashcard() throws Exception {
        when(repository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/flashcards/1"))
                .andExpect(status().isOk());

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void createFlashcard_rejectsBlankFields() throws Exception {
        when(deckRepository.findById(1L)).thenReturn(Optional.of(new Deck("General")));
        Flashcard flashcard = new Flashcard("", "");

        mockMvc.perform(post("/api/decks/1/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard)))
                .andExpect(status().isBadRequest());

        verify(repository, never()).save(any(Flashcard.class));
    }

    @Test
    void updateFlashcard_returnsNotFoundForUnknownId() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Flashcard newFlashcard = new Flashcard("newQuestion", "newAnswer");
        mockMvc.perform(put("/api/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFlashcard)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFlashcard_returnsNotFoundForUnknownId() throws Exception {
        when(repository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/flashcards/1"))
                .andExpect(status().isNotFound());

        verify(repository, never()).deleteById(anyLong());
    }
}
