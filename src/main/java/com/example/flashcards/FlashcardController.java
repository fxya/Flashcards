package com.example.flashcards;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
class FlashcardController {
    private final FlashcardRepository repository;

    FlashcardController(FlashcardRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/flashcards")
    public String getFlashcards(Model model) {
        List<Flashcard> flashcards = (List<Flashcard>) repository.findAll();
        model.addAttribute("flashcards", flashcards);
        return "index";
    }

    // POST
    @PostMapping("/flashcards")
    public Flashcard createFlashcard(Flashcard flashcard) {
        return repository.save(flashcard);
    }

    // PUT

    // GET INDIVIDUAL
    // DELETE INDIVIDUAL
}
