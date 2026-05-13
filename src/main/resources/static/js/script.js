// 1. TA FUNKCJA MUSI TU BYĆ, ŻEBY ANIMACJA DZIAŁAŁA
function typeWriter(text, element, speed, callback) {
    if (!text) return;
    let i = 0;
    element.innerHTML = "";
    element.classList.add('typing');

    function type() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(type, speed);
        } else {
            element.classList.remove('typing');
            element.style.borderRight = "none";
            if (callback) {
                setTimeout(callback, 500);
            }
        }
    }
    type();
}

document.addEventListener('DOMContentLoaded', () => {
    const titleEl = document.getElementById('title');
    const welcomeEl = document.getElementById('welcomeText');

    // Sprawdzenie czy zmienne z HTML dotarły
    if (typeof titleText === 'undefined' || titleText === null) {
        console.error("BŁĄD: titleText nie został wstrzyknięty z HTML!");
        return;
    }

    const hasAnimationPlayed = sessionStorage.getItem('animationDone');

    if (hasAnimationPlayed) {
        if (titleEl) {
            titleEl.innerHTML = titleText;
            titleEl.style.borderRight = "none";
        }
        if (welcomeEl) welcomeEl.innerHTML = descriptionText;
    } else {
        if (titleEl) {
            typeWriter(titleText, titleEl, 100, () => {
                if (welcomeEl) {
                    typeWriter(descriptionText, welcomeEl, 50, () => {
                        sessionStorage.setItem('animationDone', 'true');
                    });
                }
            });
        }
    }
});