package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class FlashcardsApplicationTests {

    @Autowired
    private FlashcardRepository repository;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }

}
