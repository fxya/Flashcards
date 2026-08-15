import * as api from './api.js';

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

function prepareForInput() {
    convertToInputTag('question');
    convertToInputTag('answer');
    toggleVisible('undo', true);
    toggleVisible('navbuttoncontainer', false);
    toggleVisible('addFlashcard', false);
    toggleVisible('editFlashcard', false);
    toggleVisible('deleteFlashcard', false);
}

function restoreAfterInput() {
    convertToPTag('question');
    convertToPTag('answer');
    toggleVisible('undo', false);
    toggleVisible('navbuttoncontainer', true);
    toggleVisible('addFlashcard', true);
    toggleVisible('editFlashcard', true);
    toggleVisible('deleteFlashcard', true);
}

function renderCurrentFlashcard() {
    const flashcard = flashcardList[currentIndex];
    document.getElementById('question').textContent =
        flashcard ? flashcard.question : 'No flashcards. Add one below.';
    document.getElementById('answer').textContent = flashcard ? flashcard.answer : '';
    toggleVisible('answer', false);
}

function handleAdd() {
    prepareForInput();
    toggleVisible('add', true);
}

function submitAdd() {
    const question = document.getElementById('question').value;
    const answer = document.getElementById('answer').value;
    api.createFlashcard({question, answer})
        .then(data => {
            flashcardList.push(data);
            currentIndex = flashcardList.length - 1;
            renderCurrentFlashcard();
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
    api.updateFlashcard(flashcardList[currentIndex].id, {question, answer})
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
    api.deleteFlashcard(flashcardList[currentIndex].id)
        .then(() => {
            flashcardList.splice(currentIndex, 1);
            currentIndex = 0;
            renderCurrentFlashcard();
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
}

function init() {
    bindEventListeners();
    api.fetchFlashcards(true)
        .then(data => {
            flashcardList = data;
            renderCurrentFlashcard();
        })
        .catch(error => console.error('Error loading flashcards:', error));
}

init();
