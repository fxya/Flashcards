CREATE TABLE flashcard (
    id SERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
);

INSERT INTO flashcard (question, answer)
VALUES ('What is the capital of the U.S.?', 'Washington, D.C.');