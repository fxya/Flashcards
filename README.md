# Flashcards

[![CI](https://github.com/fxya/Flashcards/actions/workflows/ci.yml/badge.svg)](https://github.com/fxya/Flashcards/actions/workflows/ci.yml)

Simple flashcard app in Spring Boot using Postgres for db and Thymeleaf for frontend template engine.
Flashcards are organized into decks, and each card tracks whether you know it, so study sessions
resurface the cards you got wrong before the ones you've already got down.

The frontend is server-rendered HTML: [htmx](https://htmx.org) swaps fragments in place for the
study flow (next/previous, add/edit/delete, marking a card known), plain HTML forms handle deck
management, and styling is [Tailwind CSS](https://tailwindcss.com). There's no separate JSON API and
no JS build step at runtime - htmx is vendored as a static file, and Tailwind's compiled output is
committed like any other asset.

Requires Java 21+.

| Question | Answer revealed |
| --- | --- |
| ![Flashcard showing a question](docs/screenshots/question.png) | ![Flashcard showing the revealed answer](docs/screenshots/answer.png) |

## How to run
1. Clone the repo
2. Start Postgres, schema included: `docker compose up -d`. This creates the `deck` and `flashcard`
   tables and seeds a "General" deck with one example card. No Docker? Install Postgres yourself and run
   `src/main/resources/CreateTable.sql` instead.
   - Upgrading an older installation instead? Run `Migration_AddDecks.sql` then
     `Migration_AddReviewTracking.sql` (both in `src/main/resources`) against your existing database.
3. Run the app in your IDE or with `./gradlew build` to build with tests.
4. Navigate to `localhost:8080/flashcards` in your browser.

By default the app connects to `jdbc:postgresql://localhost:5432/postgres` with username/password
`postgres`/`postgres`. Override any of these via the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
and `SPRING_DATASOURCE_PASSWORD` environment variables.

## Features
- **Decks**: group flashcards into named decks; create, switch between, and delete them from the deck
  picker (deleting a deck deletes its flashcards too, after a confirmation prompt).
- **Study order**: pick how cards are served - *Study order* prioritizes cards marked "Still Learning",
  then ones you haven't reviewed yet, then ones you've marked "I Knew It"; *Shuffle* is a plain random
  order; *In order* is deterministic by creation order.
- **Progress tracking**: reveal a card's answer to mark it "I Knew It" or "Still Learning" - the app
  timestamps the review and moves you on to the next card, with a running "Card X of N · Y known" line.
- **Search**: filter the current deck's cards by question/answer text as you type.
- **Import/export**: export a deck as a tab-separated `.txt` file compatible with Anki's plain-text
  import/export format, or import one into the current deck.

The study card is the only thing on the page meant to grab your attention - deck switching, search,
ordering, and deck/import/export management live in a quiet top bar (the "≡" menu) so they don't
compete with the card itself.

## Editing styles
Tailwind's output (`src/main/resources/static/styles.css`) is compiled from `src/main/tailwind/input.css`
and committed, so `./gradlew build` doesn't need to fetch anything extra. After changing `input.css` or
adding classes in the templates, regenerate it with:

```
./gradlew tailwindBuild
```

This downloads the [standalone Tailwind CLI](https://github.com/tailwindlabs/tailwindcss/releases) (no
Node/npm required) into `build/tailwind-cli/` on first run. The task currently targets Linux x64; on
another platform, grab the matching standalone binary and run it with the same `-i`/`-o` arguments.

## CI
Every push to `main` and every pull request runs `./gradlew build` (compile + full test suite) against a
Postgres service container via [GitHub Actions](.github/workflows/ci.yml).
