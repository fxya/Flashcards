package com.example.flashcards;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/* Deck creation and deletion are infrequent, page-level actions, so they stay
   plain HTML forms with a redirect-after-post - no htmx needed here. Deck
   switching itself is a plain GET form on the flashcards page (see
   FlashcardController#index). */

@Controller
public class DeckViewController {

    private final DeckService deckService;

    public DeckViewController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping("/decks")
    public String create(@RequestParam(required = false) Long deckId,
                          @RequestParam(defaultValue = "study") String order,
                          @Valid @ModelAttribute Deck deck, BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("deckError",
                    bindingResult.getFieldError("name").getDefaultMessage());
            return redirectTo(deckId, order);
        }
        Deck created = deckService.createDeck(deck);
        return redirectTo(created.getId(), order);
    }

    @PostMapping("/decks/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "study") String order) {
        deckService.deleteDeck(id);
        return redirectTo(null, order);
    }

    private String redirectTo(Long deckId, String order) {
        String query = deckId != null ? "?deckId=" + deckId + "&order=" + order : "?order=" + order;
        return "redirect:/flashcards" + query;
    }
}
