package com.example.flashcards;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class FlashcardService {

    private static final List<ReviewStatus> STUDY_PRIORITY =
            List.of(ReviewStatus.UNKNOWN, ReviewStatus.UNSEEN, ReviewStatus.KNOWN);
    private static final int MAX_FIELD_LENGTH = 1000;

    private final FlashcardRepository repository;
    private final DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository repository, DeckRepository deckRepository) {
        this.repository = repository;
        this.deckRepository = deckRepository;
    }

    public List<Flashcard> getFlashcards(Long deckId, String order, String search) {
        List<Flashcard> flashcards = switch (order) {
            case "shuffle" -> shuffled(repository.findByDeckId(deckId));
            case "study" -> orderForStudy(repository.findByDeckId(deckId));
            default -> repository.findByDeckIdOrderById(deckId);
        };
        return filterBySearch(flashcards, search);
    }

    private List<Flashcard> filterBySearch(List<Flashcard> flashcards, String search) {
        if (search == null || search.isBlank()) {
            return flashcards;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return flashcards.stream()
                .filter(f -> f.getQuestion().toLowerCase(Locale.ROOT).contains(needle)
                        || f.getAnswer().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    /**
     * Re-fetches a fixed sequence of flashcards by id, preserving that exact order and
     * dropping any id that no longer exists. Used to redisplay a study-session queue
     * (computed once) with live content/status on every subsequent request, without
     * re-sorting it.
     */
    public List<Flashcard> getFlashcardsById(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Flashcard> byId = new HashMap<>();
        repository.findAllById(ids).forEach(f -> byId.put(f.getId(), f));

        List<Flashcard> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Flashcard flashcard = byId.get(id);
            if (flashcard != null) {
                ordered.add(flashcard);
            }
        }
        return ordered;
    }

    private List<Flashcard> shuffled(List<Flashcard> flashcards) {
        Collections.shuffle(flashcards);
        return flashcards;
    }

    /**
     * Cards the learner got wrong resurface first, then cards never reviewed, with
     * cards already marked known deprioritized to the end. Each group is shuffled
     * internally so repeat study sessions don't always show the same sequence.
     */
    private List<Flashcard> orderForStudy(List<Flashcard> flashcards) {
        Map<ReviewStatus, List<Flashcard>> byStatus = new EnumMap<>(ReviewStatus.class);
        for (ReviewStatus status : ReviewStatus.values()) {
            byStatus.put(status, new ArrayList<>());
        }
        for (Flashcard flashcard : flashcards) {
            byStatus.get(flashcard.getStatus()).add(flashcard);
        }

        List<Flashcard> ordered = new ArrayList<>(flashcards.size());
        for (ReviewStatus status : STUDY_PRIORITY) {
            List<Flashcard> group = byStatus.get(status);
            Collections.shuffle(group);
            ordered.addAll(group);
        }
        return ordered;
    }

    public Flashcard createFlashcard(Long deckId, Flashcard flashcard) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new NoSuchElementException("No deck found with id " + deckId));
        flashcard.setDeck(deck);
        return repository.save(flashcard);
    }

    public Flashcard updateFlashcard(Long id, Flashcard flashcard) {
        var flashcardToUpdate = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No flashcard found with id " + id));
        flashcardToUpdate.setQuestion(flashcard.getQuestion());
        flashcardToUpdate.setAnswer(flashcard.getAnswer());
        return repository.save(flashcardToUpdate);
    }

    public void deleteFlashcard(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("No flashcard found with id " + id);
        }
        repository.deleteById(id);
    }

    public Flashcard reviewFlashcard(Long id, ReviewStatus status) {
        var flashcard = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No flashcard found with id " + id));
        flashcard.setStatus(status);
        flashcard.setLastReviewedAt(Instant.now());
        return repository.save(flashcard);
    }

    /**
     * Renders a deck as an Anki-compatible plain-text export: tab-separated
     * question/answer fields with the #separator/#html/#columns header comments Anki's
     * text importer recognizes (docs.ankiweb.net/importing/text-files.html), so the
     * file round-trips through Anki as well as any spreadsheet or text editor.
     */
    public String exportDeckAsText(Long deckId) {
        StringBuilder text = new StringBuilder("#separator:tab\n#html:false\n#columns:Question\tAnswer\n");
        for (Flashcard flashcard : repository.findByDeckIdOrderById(deckId)) {
            text.append(escapeField(flashcard.getQuestion()))
                    .append('\t')
                    .append(escapeField(flashcard.getAnswer()))
                    .append('\n');
        }
        return text.toString();
    }

    private String escapeField(String field) {
        return field.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    /**
     * Parses the same tab-separated format exportDeckAsText produces (and that Anki's
     * plain-text importer accepts): '#'-prefixed header/comment lines and blank lines
     * are skipped, each remaining line is split into question/answer on the first tab.
     * Rows that don't produce two valid fields are skipped rather than failing the
     * whole import. Returns the number of flashcards actually created.
     */
    public int importFromText(Long deckId, String content) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new NoSuchElementException("No deck found with id " + deckId));

        int imported = 0;
        for (String line : content.split("\r\n|\r|\n")) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\t", 2);
            if (fields.length < 2) {
                continue;
            }
            String question = fields[0].trim();
            String answer = fields[1].trim();
            if (question.isEmpty() || answer.isEmpty()
                    || question.length() > MAX_FIELD_LENGTH || answer.length() > MAX_FIELD_LENGTH) {
                continue;
            }
            repository.save(new Flashcard(question, answer, deck));
            imported++;
        }
        return imported;
    }
}
