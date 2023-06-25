package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlashcardRepository repository;
    @Test
    void getIndexPage() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(new Flashcard("question1", "answer1"),
                new Flashcard("question2", "answer2")));

        mockMvc.perform(get("/flashcards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/html;charset=UTF-8")))
                .andExpect(model().attributeExists("flashcardList"));

        verify(repository, times(1)).findAll();
    }
}