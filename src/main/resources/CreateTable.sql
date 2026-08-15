CREATE TABLE deck (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE flashcard (
    id SERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    deck_id BIGINT NOT NULL REFERENCES deck(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'UNSEEN',
    last_reviewed_at TIMESTAMPTZ
);

INSERT INTO deck (name) VALUES ('General');

INSERT INTO flashcard (question, answer, deck_id)
VALUES ('What is the capital of the U.S.?', 'Washington, D.C.', (SELECT id FROM deck WHERE name = 'General'));
