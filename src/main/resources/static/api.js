const BASE_URL = '/api/flashcards';

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

export function fetchFlashcards(shuffle = false) {
    return request(`${BASE_URL}?shuffle=${shuffle}`);
}

export function createFlashcard(flashcard) {
    return request(BASE_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    });
}

export function updateFlashcard(id, flashcard) {
    return request(`${BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    });
}

export function deleteFlashcard(id) {
    return request(`${BASE_URL}/${id}`, {method: 'DELETE'});
}
