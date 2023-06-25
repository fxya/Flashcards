package com.example.flashcards;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/* This controller serves the HTML page that displays the flashcards. There is
a single endpoint, /flashcards, that returns the index.html template. */

@Controller
class FlashcardController {
    private final FlashcardRepository repository;

    FlashcardController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/flashcards")
    public String getFlashcards(Model model) {
        var flashcards = (List<Flashcard>) repository.findAll();
        model.addAttribute("flashcardList", flashcards);
        return "index";
    }

}
