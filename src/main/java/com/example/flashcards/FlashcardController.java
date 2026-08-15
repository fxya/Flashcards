package com.example.flashcards;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* Serves the flashcards page and the study-region fragment that htmx swaps for
   every in-session interaction (next/previous, add/edit/delete, review, mode
   toggling, live search). Deck selection and deck management stay plain HTML
   forms - see DeckViewController - since those are infrequent, page-level
   actions. */

@Controller
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final DeckService deckService;

    public FlashcardController(FlashcardService flashcardService, DeckService deckService) {
        this.flashcardService = flashcardService;
        this.deckService = deckService;
    }

    /**
     * Serves both the full page (a normal browser navigation - deck/order switch, or
     * the initial load) and, when htmx makes the request (live search-as-you-type),
     * just the study fragment - same query params, same queue-generation logic,
     * different response shape.
     */
    @GetMapping("/flashcards")
    public String index(@RequestParam(required = false) Long deckId,
                         @RequestParam(defaultValue = "study") String order,
                         @RequestParam(required = false) String search,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                         Model model) {
        List<DeckSummary> decks = deckService.getDecks();
        Long resolvedDeckId = resolveDeckId(decks, deckId);
        model.addAttribute("decks", decks);
        model.addAttribute("selectedDeckId", resolvedDeckId);
        model.addAttribute("selectedDeck", findDeck(decks, resolvedDeckId));

        List<Long> queue = resolvedDeckId == null
                ? List.of()
                : flashcardService.getFlashcards(resolvedDeckId, order, search).stream().map(Flashcard::getId).toList();
        populateStudyModel(model, resolvedDeckId, order, search, queue, 0, "view", null, null, null);
        return hxRequest != null ? "fragments/study :: study" : "index";
    }

    @GetMapping("/flashcards/study/next")
    public String next(@RequestParam Long deckId, @RequestParam String order, @RequestParam(required = false) String search,
                        @RequestParam String queue, @RequestParam int index, Model model) {
        List<Long> ids = parseQueue(queue);
        int nextIndex = clamp(index + 1, ids.size());
        populateStudyModel(model, deckId, order, search, ids, nextIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    @GetMapping("/flashcards/study/previous")
    public String previous(@RequestParam Long deckId, @RequestParam String order, @RequestParam(required = false) String search,
                            @RequestParam String queue, @RequestParam int index, Model model) {
        List<Long> ids = parseQueue(queue);
        int prevIndex = clamp(index - 1, ids.size());
        populateStudyModel(model, deckId, order, search, ids, prevIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    @GetMapping("/flashcards/study/mode")
    public String changeMode(@RequestParam Long deckId, @RequestParam String order, @RequestParam(required = false) String search,
                              @RequestParam String queue, @RequestParam int index,
                              @RequestParam String mode, Model model) {
        List<Long> ids = parseQueue(queue);
        populateStudyModel(model, deckId, order, search, ids, clamp(index, ids.size()), mode, null, null, null);
        return "fragments/study :: study";
    }

    @PostMapping("/flashcards/{id}/review")
    public String review(@PathVariable Long id, @RequestParam Long deckId, @RequestParam String order,
                          @RequestParam(required = false) String search,
                          @RequestParam String queue, @RequestParam int index,
                          @RequestParam ReviewStatus status, Model model) {
        flashcardService.reviewFlashcard(id, status);
        List<Long> ids = parseQueue(queue);
        int nextIndex = clamp(index + 1, ids.size());
        populateStudyModel(model, deckId, order, search, ids, nextIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    @PostMapping("/flashcards/study/create")
    public String create(@RequestParam Long deckId, @RequestParam String order, @RequestParam(required = false) String search,
                          @RequestParam String queue, @RequestParam int index,
                          @Valid @ModelAttribute Flashcard flashcard, BindingResult bindingResult,
                          Model model) {
        List<Long> ids = parseQueue(queue);
        if (bindingResult.hasErrors()) {
            // Redisplay the same card/queue the user was on - don't reshuffle just because
            // the new-card form failed validation.
            populateStudyModel(model, deckId, order, search, ids, clamp(index, ids.size()), "add",
                    fieldErrors(bindingResult), flashcard.getQuestion(), flashcard.getAnswer());
            return "fragments/study :: study";
        }
        Flashcard created = flashcardService.createFlashcard(deckId, flashcard);
        List<Long> freshIds = flashcardService.getFlashcards(deckId, order, search).stream().map(Flashcard::getId).toList();
        int newIndex = Math.max(freshIds.indexOf(created.getId()), 0);
        populateStudyModel(model, deckId, order, search, freshIds, newIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    @PostMapping("/flashcards/{id}/edit")
    public String edit(@PathVariable Long id, @RequestParam Long deckId, @RequestParam String order,
                        @RequestParam(required = false) String search,
                        @RequestParam String queue, @RequestParam int index,
                        @Valid @ModelAttribute Flashcard flashcard, BindingResult bindingResult,
                        Model model) {
        List<Long> ids = parseQueue(queue);
        int clampedIndex = clamp(index, ids.size());
        if (bindingResult.hasErrors()) {
            populateStudyModel(model, deckId, order, search, ids, clampedIndex, "edit",
                    fieldErrors(bindingResult), flashcard.getQuestion(), flashcard.getAnswer());
            return "fragments/study :: study";
        }
        flashcardService.updateFlashcard(id, flashcard);
        populateStudyModel(model, deckId, order, search, ids, clampedIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    @DeleteMapping("/flashcards/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long deckId, @RequestParam String order,
                          @RequestParam(required = false) String search,
                          @RequestParam String queue, @RequestParam int index, Model model) {
        flashcardService.deleteFlashcard(id);
        List<Long> ids = new ArrayList<>(parseQueue(queue));
        ids.remove(id);
        int newIndex = ids.isEmpty() ? 0 : Math.min(index, ids.size() - 1);
        populateStudyModel(model, deckId, order, search, ids, newIndex, "view", null, null, null);
        return "fragments/study :: study";
    }

    // ---- helpers ----

    private Long resolveDeckId(List<DeckSummary> decks, Long requested) {
        if (decks.isEmpty()) {
            return null;
        }
        return decks.stream().anyMatch(d -> d.id().equals(requested)) ? requested : decks.get(0).id();
    }

    private DeckSummary findDeck(List<DeckSummary> decks, Long deckId) {
        return decks.stream().filter(d -> d.id().equals(deckId)).findFirst().orElse(null);
    }

    private List<Long> parseQueue(String queue) {
        if (queue == null || queue.isBlank()) {
            return List.of();
        }
        return List.of(queue.split(",")).stream().map(Long::valueOf).toList();
    }

    private int clamp(int index, int size) {
        if (size == 0) {
            return 0;
        }
        return Math.max(0, Math.min(index, size - 1));
    }

    private Map<String, String> fieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return errors;
    }

    /**
     * Reconstitutes the ordered flashcard list from a queue of ids (re-fetching live
     * data, so status/content updates always show even though the id order is fixed
     * for the browsing session), and populates everything the study fragment needs.
     */
    private void populateStudyModel(Model model, Long deckId, String order, String search, List<Long> queue, int index,
                                     String mode, Map<String, String> errors, String formQuestion, String formAnswer) {
        boolean hasDeck = deckId != null;
        List<Flashcard> flashcards = hasDeck ? flashcardService.getFlashcardsById(queue) : List.of();
        Flashcard current = flashcards.isEmpty() ? null : flashcards.get(clamp(index, flashcards.size()));
        long knownCount = flashcards.stream().filter(f -> f.getStatus() == ReviewStatus.KNOWN).count();

        model.addAttribute("hasDeck", hasDeck);
        model.addAttribute("deckId", deckId);
        model.addAttribute("order", order);
        model.addAttribute("search", search);
        model.addAttribute("flashcards", flashcards);
        model.addAttribute("currentFlashcard", current);
        model.addAttribute("index", flashcards.isEmpty() ? 0 : clamp(index, flashcards.size()));
        model.addAttribute("queueParam", flashcards.stream().map(Flashcard::getId).map(String::valueOf)
                .collect(Collectors.joining(",")));
        model.addAttribute("mode", mode);
        model.addAttribute("errors", errors);
        model.addAttribute("knownCount", knownCount);

        boolean prefillFromCurrent = formQuestion == null && formAnswer == null;
        model.addAttribute("formQuestion",
                prefillFromCurrent ? (current != null ? current.getQuestion() : "") : formQuestion);
        model.addAttribute("formAnswer",
                prefillFromCurrent ? (current != null ? current.getAnswer() : "") : formAnswer);
    }
}
