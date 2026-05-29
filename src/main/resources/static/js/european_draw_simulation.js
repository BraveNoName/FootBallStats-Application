document.addEventListener("DOMContentLoaded", () => {
    const slides = document.querySelectorAll(".team-draw-slide");
    const dots = document.querySelectorAll(".club-selector-btn");
    const prevBtn = document.getElementById("drawPrevBtn");
    const nextBtn = document.getElementById("drawNextBtn");

    if (!slides.length) return;

    let currentIndex = 0;

    function showSlide(index) {
        slides[currentIndex].classList.remove("active");
        dots[currentIndex]?.classList.remove("active");

        currentIndex = (index + slides.length) % slides.length;

        slides[currentIndex].classList.add("active");
        dots[currentIndex]?.classList.add("active");
    }

    prevBtn?.addEventListener("click", () => {
        showSlide(currentIndex - 1);
    });

    nextBtn?.addEventListener("click", () => {
        showSlide(currentIndex + 1);
    });

    dots.forEach((dot, index) => {
        dot.addEventListener("click", () => {
            showSlide(index);
        });
    });
});