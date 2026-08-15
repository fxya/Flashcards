-- Migration for installations that already have Migration_AddDecks.sql applied
-- but predate per-card review tracking (known/unknown marking, study order).
-- Safe to run once.

ALTER TABLE flashcard ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'UNSEEN';
ALTER TABLE flashcard ADD COLUMN IF NOT EXISTS last_reviewed_at TIMESTAMPTZ;
