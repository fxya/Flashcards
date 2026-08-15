# Flashcards

[![CI](https://github.com/fxya/Flashcards/actions/workflows/ci.yml/badge.svg)](https://github.com/fxya/Flashcards/actions/workflows/ci.yml)

Simple flashcard app in Spring Boot using Postgres for db and Thymeleaf for frontend template engine.
Flashcards are organized into decks, and each card tracks whether you know it, so study sessions
resurface the cards you got wrong before the ones you've already got down.

Requires Java 21+.

| Question | Answer revealed |
| --- | --- |
| ![Flashcard showing a question](docs/screenshots/question.png) | ![Flashcard showing the revealed answer](docs/screenshots/answer.png) |

## How to run
1. Clone the repo
2. Install Postgres and run `src/main/resources/CreateTable.sql`. This creates the `deck` and `flashcard`
   tables and seeds a "General" deck with one example card.
   - Upgrading an older installation instead? Run `Migration_AddDecks.sql` then
     `Migration_AddReviewTracking.sql` (both in `src/main/resources`) against your existing database.
3. Run the app in your IDE or with `./gradlew build` to build with tests.
4. Navigate to `localhost:8080/flashcards` in your browser.

By default the app connects to `jdbc:postgresql://localhost:5432/postgres` with username/password
`postgres`/`postgres`. Override any of these via the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
and `SPRING_DATASOURCE_PASSWORD` environment variables.

## Features
- **Decks**: group flashcards into named decks; create, switch between, and delete them from the deck bar
  above the card (deleting a deck deletes its flashcards too, after a confirmation prompt).
- **Study order**: pick how cards are served - *Study order* prioritizes cards marked "Still Learning",
  then ones you haven't reviewed yet, then ones you've marked "I Knew It"; *Shuffle* is a plain random
  order; *In order* is deterministic by creation order.
- **Progress tracking**: reveal a card's answer to mark it "I Knew It" or "Still Learning" - the app
  timestamps the review and moves you on to the next card, with a running "Card X of N · Y known" line.

## CI
Every push to `main` and every pull request runs `./gradlew build` (compile + full test suite) against a
Postgres service container via [GitHub Actions](.github/workflows/ci.yml).
