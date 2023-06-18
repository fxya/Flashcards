let flashcardList = [];
let currentIndex = 0;

function updateFlashcard() {
    if (flashcardList[currentIndex]) {
        document.getElementById('question').textContent = flashcardList[currentIndex].question;
        document.getElementById('answer').textContent = flashcardList[currentIndex].answer;
        document.getElementById('answer').style.display = 'none'; // Hide the answer by default
    }
}

function enableAddFlashcard() {
    document.getElementById('question').outerHTML = '<input type="text" id="question" placeholder="Enter question">';
    document.getElementById('answer').outerHTML = '<input type="text" id="answer" placeholder="Enter answer">';
    document.getElementById('add').style.display = 'inline'; // Show the 'Add' button
    document.getElementById('addFlashcard').style.display = 'none'; // Hide the 'Add Flashcard' button
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
    document.getElementById('question').outerHTML = '<p id="question"></p>'; // Convert back to p tag after adding
    document.getElementById('answer').outerHTML = '<p id="answer"></p>'; // Convert back to p tag after adding
    document.getElementById('add').style.display = 'none'; // Hide the 'Add' button
    document.getElementById('addFlashcard').style.display = 'inline'; // Show the 'Add Flashcard' button
    updateFlashcard();
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
    document.getElementById('answer').style.display = 'block';
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
