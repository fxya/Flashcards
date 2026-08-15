import * as api from './api.js';

let decks = [];
let currentDeckId = null;
let orderMode = 'study';
let flashcardList = [];
let currentIndex = 0;

function toggleVisible(id, visible) {
    document.getElementById(id).classList.toggle('hidden', !visible);
}

function convertToInputTag(id) {
    const element = document.getElementById(id);
    const input = document.createElement('input');
    input.type = 'text';
    input.id = id;
    input.value = element.textContent;
    element.replaceWith(input);
}

function convertToPTag(id) {
    const element = document.getElementById(id);
    const p = document.createElement('p');
    p.id = id;
    p.textContent = element.value;
    element.replaceWith(p);
}

function setDeckSwitchingEnabled(enabled) {
    document.getElementById('deckSelect').disabled = !enabled;
    document.getElementById('newDeckButton').disabled = !enabled;
    document.getElementById('deleteDeckButton').disabled = !enabled;
    document.getElementById('orderSelect').disabled = !enabled;
}

function prepareForInput() {
    convertToInputTag('question');
    convertToInputTag('answer');
    toggleVisible('undo', true);
    toggleVisible('navbuttoncontainer', false);
    toggleVisible('addFlashcard', false);
    toggleVisible('editFlashcard', false);
    toggleVisible('deleteFlashcard', false);
    setDeckSwitchingEnabled(false);
}

function restoreAfterInput() {
    convertToPTag('question');
    convertToPTag('answer');
    toggleVisible('undo', false);
    toggleVisible('navbuttoncontainer', true);
    toggleVisible('addFlashcard', true);
    toggleVisible('editFlashcard', true);
    toggleVisible('deleteFlashcard', true);
    setDeckSwitchingEnabled(true);
}

function currentFlashcard() {
    return flashcardList[currentIndex];
}

function renderCurrentFlashcard() {
    const flashcard = currentFlashcard();
    document.getElementById('question').textContent =
        flashcard ? flashcard.question : 'No flashcards. Add one below.';
    document.getElementById('answer').textContent = flashcard ? flashcard.answer : '';
    toggleVisible('answer', false);
    toggleVisible('reviewButtons', false);
    updateProgress();
}

function updateProgress() {
    const progress = document.getElementById('progress');
    if (flashcardList.length === 0) {
        progress.textContent = '';
        return;
    }
    const knownCount = flashcardList.filter(f => f.status === 'KNOWN').length;
    progress.textContent = `Card ${currentIndex + 1} of ${flashcardList.length} · ${knownCount} known`;
}

function setNoDecksState() {
    currentDeckId = null;
    flashcardList = [];
    currentIndex = 0;
    document.getElementById('deckSelect').innerHTML = '';
    document.getElementById('question').textContent = 'No decks yet. Create one above to get started.';
    document.getElementById('answer').textContent = '';
    toggleVisible('answer', false);
    toggleVisible('navbuttoncontainer', false);
    toggleVisible('reviewButtons', false);
    document.getElementById('progress').textContent = '';
    document.getElementById('addFlashcard').disabled = true;
    document.getElementById('editFlashcard').disabled = true;
    document.getElementById('deleteFlashcard').disabled = true;
    document.getElementById('deleteDeckButton').disabled = true;
    document.getElementById('orderSelect').disabled = true;
}

function populateDeckSelect() {
    const select = document.getElementById('deckSelect');
    select.innerHTML = '';
    decks.forEach(deck => {
        const option = document.createElement('option');
        option.value = deck.id;
        option.textContent = `${deck.name} (${deck.cardCount})`;
        select.appendChild(option);
    });
    select.value = currentDeckId;
}

async function refreshDeckCounts() {
    decks = await api.fetchDecks();
    populateDeckSelect();
}

async function loadDecks(selectDeckId) {
    decks = await api.fetchDecks();

    if (decks.length === 0) {
        setNoDecksState();
        return;
    }

    const wanted = selectDeckId ?? currentDeckId;
    currentDeckId = decks.some(deck => deck.id === wanted) ? wanted : decks[0].id;
    populateDeckSelect();
    toggleVisible('navbuttoncontainer', true);
    document.getElementById('addFlashcard').disabled = false;
    document.getElementById('editFlashcard').disabled = false;
    document.getElementById('deleteFlashcard').disabled = false;
    document.getElementById('deleteDeckButton').disabled = false;
    document.getElementById('orderSelect').disabled = false;

    await loadFlashcards();
}

async function loadFlashcards() {
    flashcardList = await api.fetchFlashcards(currentDeckId, orderMode);
    currentIndex = 0;
    renderCurrentFlashcard();
}

function handleAdd() {
    prepareForInput();
    toggleVisible('add', true);
}

function submitAdd() {
    const question = document.getElementById('question').value;
    const answer = document.getElementById('answer').value;
    api.createFlashcard(currentDeckId, {question, answer})
        .then(data => {
            flashcardList.push(data);
            currentIndex = flashcardList.length - 1;
            renderCurrentFlashcard();
            return refreshDeckCounts();
        })
        .catch(error => console.error('Error adding flashcard:', error));
    restoreAfterInput();
    toggleVisible('add', false);
}

function handleEdit() {
    prepareForInput();
    toggleVisible('edit', true);
}

function submitEdit() {
    const question = document.getElementById('question').value;
    const answer = document.getElementById('answer').value;
    api.updateFlashcard(currentFlashcard().id, {question, answer})
        .then(data => {
            flashcardList[currentIndex] = data;
            renderCurrentFlashcard();
        })
        .catch(error => console.error('Error updating flashcard:', error));
    restoreAfterInput();
    toggleVisible('edit', false);
}

function handleDelete() {
    prepareForInput();
    toggleVisible('delete', true);
}

function submitDelete() {
    api.deleteFlashcard(currentFlashcard().id)
        .then(() => {
            flashcardList.splice(currentIndex, 1);
            currentIndex = 0;
            renderCurrentFlashcard();
            return refreshDeckCounts();
        })
        .catch(error => console.error('Error deleting flashcard:', error));
    restoreAfterInput();
    toggleVisible('delete', false);
}

function handleUndo() {
    renderCurrentFlashcard();
    restoreAfterInput();
    toggleVisible('add', false);
    toggleVisible('edit', false);
    toggleVisible('delete', false);
}

function showNext() {
    if (currentIndex < flashcardList.length - 1) {
        currentIndex++;
        renderCurrentFlashcard();
    }
}

function showPrevious() {
    if (currentIndex > 0) {
        currentIndex--;
        renderCurrentFlashcard();
    }
}

function revealAnswer() {
    toggleVisible('answer', true);
    toggleVisible('reviewButtons', true);
}

function submitReview(status) {
    const flashcard = currentFlashcard();
    if (!flashcard) {
        return;
    }
    api.reviewFlashcard(flashcard.id, status)
        .then(updated => {
            flashcardList[currentIndex] = updated;
            if (currentIndex < flashcardList.length - 1) {
                currentIndex++;
            }
            renderCurrentFlashcard();
        })
        .catch(error => console.error('Error reviewing flashcard:', error));
}

function handleDeckChange(event) {
    currentDeckId = Number(event.target.value);
    loadFlashcards().catch(error => console.error('Error loading flashcards:', error));
}

function handleOrderChange(event) {
    orderMode = event.target.value;
    loadFlashcards().catch(error => console.error('Error loading flashcards:', error));
}

function showNewDeckForm() {
    toggleVisible('newDeckForm', true);
    document.getElementById('newDeckName').focus();
}

function hideNewDeckForm() {
    toggleVisible('newDeckForm', false);
    document.getElementById('newDeckName').value = '';
}

function submitNewDeck() {
    const name = document.getElementById('newDeckName').value.trim();
    if (!name) {
        return;
    }
    api.createDeck(name)
        .then(deck => {
            hideNewDeckForm();
            return loadDecks(deck.id);
        })
        .catch(error => console.error('Error creating deck:', error));
}

function submitDeleteDeck() {
    if (currentDeckId == null) {
        return;
    }
    const deck = decks.find(d => d.id === currentDeckId);
    const confirmed = window.confirm(
        `Delete "${deck ? deck.name : 'this deck'}" and all ${deck ? deck.cardCount : 0} of its flashcards? This cannot be undone.`
    );
    if (!confirmed) {
        return;
    }
    api.deleteDeck(currentDeckId)
        .then(() => loadDecks())
        .catch(error => console.error('Error deleting deck:', error));
}

function bindEventListeners() {
    document.getElementById('prevButton').addEventListener('click', showPrevious);
    document.getElementById('revealButton').addEventListener('click', revealAnswer);
    document.getElementById('nextButton').addEventListener('click', showNext);
    document.getElementById('addFlashcard').addEventListener('click', handleAdd);
    document.getElementById('editFlashcard').addEventListener('click', handleEdit);
    document.getElementById('deleteFlashcard').addEventListener('click', handleDelete);
    document.getElementById('add').addEventListener('click', submitAdd);
    document.getElementById('edit').addEventListener('click', submitEdit);
    document.getElementById('delete').addEventListener('click', submitDelete);
    document.getElementById('undo').addEventListener('click', handleUndo);
    document.getElementById('knownButton').addEventListener('click', () => submitReview('KNOWN'));
    document.getElementById('unknownButton').addEventListener('click', () => submitReview('UNKNOWN'));
    document.getElementById('deckSelect').addEventListener('change', handleDeckChange);
    document.getElementById('orderSelect').addEventListener('change', handleOrderChange);
    document.getElementById('newDeckButton').addEventListener('click', showNewDeckForm);
    document.getElementById('cancelDeckButton').addEventListener('click', hideNewDeckForm);
    document.getElementById('createDeckButton').addEventListener('click', submitNewDeck);
    document.getElementById('deleteDeckButton').addEventListener('click', submitDeleteDeck);
}

function init() {
    bindEventListeners();
    loadDecks().catch(error => console.error('Error loading decks:', error));
}

init();
