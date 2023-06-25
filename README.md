# Flashcards
Simple flashcard app in Spring using Postgres for db and Thymeleaf for frontend template engine.

## How to run
1. Clone the repo
2. Install Postgres and run the CREATE statement in `CreateTable.sql`. This will create the database and table. 
Optionally populate the table as shown with the INSERT example in CreateTable.sql.
3. Run the app in your IDE or with `./gradlew build` to build with tests.
4. Navigate to `localhost:8080/flashcards` in your browser.