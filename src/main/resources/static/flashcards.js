let flashcardList = [];
let currentIndex = 0;

function updateFlashcard() {
    if (flashcardList[currentIndex]) {
        document.getElementById('question').textContent = flashcardList[currentIndex].question;
        document.getElementById('answer').textContent = flashcardList[currentIndex].answer;
    }
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

window.onload = function() {
    fetch('api/flashcards')
        .then(response => response.json())
        .then(data => {
            flashcardList = data;
            updateFlashcard();
        })
        .catch(error => console.error('Error:', error));
};
