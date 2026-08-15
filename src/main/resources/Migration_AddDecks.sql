-- Migration for existing installations created before decks were introduced.
-- Adds a deck table, assigns every existing flashcard to a new "General"
-- deck, and makes deck_id required going forward. Safe to run once.

CREATE TABLE IF NOT EXISTS deck (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO deck (name)
SELECT 'General'
WHERE NOT EXISTS (SELECT 1 FROM deck WHERE name = 'General');

ALTER TABLE flashcard ADD COLUMN IF NOT EXISTS deck_id BIGINT REFERENCES deck(id) ON DELETE CASCADE;

UPDATE flashcard
SET deck_id = (SELECT id FROM deck WHERE name = 'General')
WHERE deck_id IS NULL;

ALTER TABLE flashcard ALTER COLUMN deck_id SET NOT NULL;
