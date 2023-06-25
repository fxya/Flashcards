let flashcardList = [];
let currentIndex = 0;

function convertToInputTag(id) {
    let element = document.getElementById(id);
    let value = element.textContent;
    element.outerHTML = `<input type="text" id="${id}" value="${value}">`;
}

function convertToPTag(id) {
    let element = document.getElementById(id);
    let value = element.value;
    element.outerHTML = `<p id="${id}">${value}</p>`;
}

function toggleDisplay(id, state) {
    document.getElementById(id).style.display = state ? 'flex' : 'none';
}

function prepareForInput() {
    convertToInputTag('question');
    convertToInputTag('answer');
    toggleDisplay('undo', true)
    toggleDisplay('navbuttoncontainer', false);
}

function restoreAfterInput() {
    convertToPTag('question');
    convertToPTag('answer');
    toggleDisplay('undo', false)
    toggleDisplay('navbuttoncontainer', true);
}

function undo() {
    updateFlashcard();
    restoreAfterInput();
    toggleDisplay('add', false);
    toggleDisplay('edit', false);
    toggleDisplay('addFlashcard', true);
    toggleDisplay('editFlashcard', true);
}

function updateFlashcard() {
    if (flashcardList[currentIndex]) {
        document.getElementById('question').textContent = flashcardList[currentIndex].question;
        document.getElementById('answer').textContent = flashcardList[currentIndex].answer;
        toggleDisplay('answer', false);
    }
}

function enableAddFlashcard() {
    prepareForInput();
    toggleDisplay('add', true);
    toggleDisplay('addFlashcard', false);
    toggleDisplay('editFlashcard', false);
}

function addFlashcard() {
    const question = document.getElementById('question').value;
    const answer = document.getElementById('answer').value;
    const flashcard = {question: question, answer: answer};
    fetch('api/flashcards', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    })
        .then(response => response.json())
        .then(data => {
            flashcardList.push(data);
            currentIndex = flashcardList.length - 1;
            updateFlashcard();
        })
        .catch(error => console.error('Error:', error));

    restoreAfterInput();
    toggleDisplay('add', false);
    toggleDisplay('addFlashcard', true);
    toggleDisplay('editFlashcard', true);
}

function enableEditFlashcard() {
    prepareForInput();
    toggleDisplay('edit', true);
    toggleDisplay('editFlashcard', false);
    toggleDisplay('addFlashcard', false);
}

function editFlashcard() {
    const question = document.getElementById('question').value;
    const answer = document.getElementById('answer').value;
    const flashcard = {question: question, answer: answer};
    fetch('api/flashcards/' + flashcardList[currentIndex].id, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(flashcard)
    })
        .then(response => response.json())
        .then(data => {
            flashcardList[currentIndex] = data;
            updateFlashcard();
        })
        .catch(error => console.error('Error:', error));

    restoreAfterInput();
    toggleDisplay('edit', false);
    toggleDisplay('editFlashcard', true);
    toggleDisplay('addFlashcard', true);
}

function nextFlashcard() {
    if (currentIndex < flashcardList.length - 1) {
        currentIndex++;
        updateFlashcard();
    }
}

function prevFlashcard() {
    if (currentIndex > 0) {
        currentIndex--;
        updateFlashcard();
    }
}

function revealAnswer() {
    toggleDisplay('answer', true);
}

window.onload = function() {
    fetch('api/flashcards')
        .then(response => response.json())
        .then(data => {
            flashcardList = data;
            updateFlashcard();
        })
        .catch(error => console.error('Error:', error));
};
