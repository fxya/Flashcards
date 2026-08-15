package com.example.flashcards;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* Deck creation, deletion, and import/export are infrequent, page-level actions, so
   they stay plain HTML forms with a redirect-after-post - no htmx needed here. Deck
   switching itself is a plain GET form on the flashcards page (see
   FlashcardController#index). */

@Controller
public class DeckViewController {

    private final DeckService deckService;
    private final FlashcardService flashcardService;

    public DeckViewController(DeckService deckService, FlashcardService flashcardService) {
        this.deckService = deckService;
        this.flashcardService = flashcardService;
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

    @GetMapping("/decks/{id}/export")
    public ResponseEntity<String> export(@PathVariable Long id) {
        Deck deck = deckService.getDeck(id);
        String content = flashcardService.exportDeckAsText(id);
        String filename = sanitizeFilename(deck.getName()) + ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                .body(content);
    }

    @PostMapping("/decks/{id}/import")
    public String importCards(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                               @RequestParam(defaultValue = "study") String order,
                               RedirectAttributes redirectAttributes) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int imported = flashcardService.importFromText(id, content);
        redirectAttributes.addFlashAttribute("importMessage",
                "Imported " + imported + " card" + (imported == 1 ? "" : "s") + ".");
        return redirectTo(id, order);
    }

    private String sanitizeFilename(String name) {
        String cleaned = name.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        return cleaned.isEmpty() ? "deck" : cleaned;
    }

    private String redirectTo(Long deckId, String order) {
        String query = deckId != null ? "?deckId=" + deckId + "&order=" + order : "?order=" + order;
        return "redirect:/flashcards" + query;
    }
}
