const DECKS_URL = '/api/decks';
const FLASHCARDS_URL = '/api/flashcards';

async function request(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || `Request failed with status ${response.status}`);
    }
    if (response.status === 204 || response.headers.get('content-length') === '0') {
        return null;
    }
    return response.json();
}

export function fetchDecks() {
    return request(DECKS_URL);
}

export function createDeck(name) {
    return request(DECKS_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({name})
    });
}

export function deleteDeck(id) {
    return request(`${DECKS_URL}/${id}`, {method: 'DELETE'});
}

export function fetchFlashcards(deckId, order = 'study') {
    return request(`${DECKS_URL}/${deckId}/flashcards?order=${order}`);
}

export function createFlashcard(deckId, flashcard) {
    return request(`${DECKS_URL}/${deckId}/flashcards`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    });
}

export function updateFlashcard(id, flashcard) {
    return request(`${FLASHCARDS_URL}/${id}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    });
}

export function deleteFlashcard(id) {
    return request(`${FLASHCARDS_URL}/${id}`, {method: 'DELETE'});
}

export function reviewFlashcard(id, status) {
    return request(`${FLASHCARDS_URL}/${id}/review`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({status})
    });
}
