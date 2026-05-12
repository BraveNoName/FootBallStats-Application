const titleEl = document.getElementById('title');
const welcomeEl = document.getElementById('welcomeText');

const titleText = "FootballStats System";
const descriptionText = "Analyze matches, players and teams in one modern football analytics system.";

function typeWriter(text, element, speed, callback) {
    let i = 0;
    element.classList.add('typing'); // Dodaj migający kursor

    function type() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(type, speed);
        } else {
            element.classList.remove('typing'); // Usuń kursor po zakończeniu
            element.style.borderRight = "none"; // Schowaj kreskę
            if (callback) {
                setTimeout(callback, 500); // Mała pauza przed następnym tekstem
            }
        }
    }
    type();
}

// Uruchomienie sekwencji po załadowaniu strony
document.addEventListener('DOMContentLoaded', () => {
    typeWriter(titleText, titleEl, 100, () => {
        typeWriter(descriptionText, welcomeEl, 50);
    });
});