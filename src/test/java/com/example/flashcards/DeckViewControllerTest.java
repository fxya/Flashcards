package com.example.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void exportProducesAnkiCompatibleTabSeparatedText() throws Exception {
        Deck deck = deckRepository.save(new Deck("Capitals"));
        flashcardRepository.save(new Flashcard("Capital of France?", "Paris", deck));

        mockMvc.perform(get("/decks/{id}/export", deck.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Capitals.txt")))
                .andExpect(content().string(containsString("#separator:tab")))
                .andExpect(content().string(containsString("Capital of France?\tParis")));
    }

    @Test
    void importCreatesCardsAndSkipsMalformedLines() throws Exception {
        Deck deck = deckRepository.save(new Deck("Imported"));
        String text = "#separator:tab\n#html:false\n#columns:Question\tAnswer\n"
                + "Capital of Japan?\tTokyo\n"
                + "not a valid line without a tab\n"
                + "\t\n"
                + "2+2?\t4\n";
        MockMultipartFile file = new MockMultipartFile("file", "cards.txt", "text/plain",
                text.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/decks/{id}/import", deck.getId()).file(file).param("order", "study"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/flashcards?deckId=" + deck.getId() + "&order=study"))
                .andExpect(flash().attribute("importMessage", "Imported 2 cards."));

        assertThat(flashcardRepository.countByDeckId(deck.getId())).isEqualTo(2);
        assertThat(flashcardRepository.findByDeckIdOrderById(deck.getId()))
                .extracting(Flashcard::getQuestion)
                .containsExactly("Capital of Japan?", "2+2?");
    }
}
