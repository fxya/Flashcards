package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FlashcardAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlashcardRepository repository;

    @Autowired
    private ObjectMapper objectMapper; //for object to JSON conversion

    @Test
    void getFlashcards() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(new Flashcard("question1", "answer1"), new Flashcard("question2", "answer2")));

        mockMvc.perform(get("/api/flashcards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2));

        verify(repository, times(2)).findAll();
    }

    @Test
    void createFlashcard() throws Exception {
        Flashcard flashcard = new Flashcard("question", "answer");
        when(repository.save(any(Flashcard.class))).thenReturn(flashcard);

        mockMvc.perform(post("/api/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard)))
                .andExpect(status().isOk());

        verify(repository, times(1)).save(any(Flashcard.class));
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
        Flashcard existingFlashcard = new Flashcard("question", "answer");
        when(repository.findById(1L)).thenReturn(Optional.of(existingFlashcard));

        mockMvc.perform(delete("/api/flashcards/1"))
                .andExpect(status().isOk());

        verify(repository, times(1)).deleteById(1L);
    }
}
