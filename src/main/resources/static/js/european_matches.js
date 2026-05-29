document.addEventListener("DOMContentLoaded", function () {
    const page = document.querySelector(".european-page");
    const competition = page?.dataset.competition || "";
    const trophy = document.getElementById("competitionTrophy");
    const intro = document.getElementById("competitionIntro");

    const config = {
        "champions-league": {
            bodyClass: "ucl-theme",
            trophy: "/Images/european_leagues/UCL_Trophy.png",
            intro: "/Images/european_leagues/lm_intro.mp4"
        },
        "europa-league": {
            bodyClass: "uel-theme",
            trophy: "/Images/european_leagues/UEL_Trophy.png",
            intro: "/Images/european_leagues/le_intro.mp4"
        },
        "conference-league": {
            bodyClass: "uecl-theme",
            trophy: "/Images/european_leagues/UECL_Trophy.png",
            intro: "/Images/european_leagues/cl_intro.mp4"
        }
    };

    const selected = config[competition] || config["champions-league"];

    document.body.classList.add(selected.bodyClass);

    if (trophy) {
        trophy.src = selected.trophy;
    }

    if (intro) {
        intro.src = selected.intro;
        intro.load();
        intro.play().catch(() => {});
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const slides = document.querySelectorAll(".league-round-slide");
    const prevBtn = document.getElementById("prevRoundBtn");
    const nextBtn = document.getElementById("nextRoundBtn");
    const title = document.getElementById("roundSliderTitle");

    if (!slides.length || !prevBtn || !nextBtn || !title) {
        return;
    }

    let currentIndex = 0;

    function showSlide(index) {
        slides.forEach(slide => slide.classList.remove("active"));

        slides[index].classList.add("active");

        const roundNumber = slides[index].dataset.round;
        title.textContent = `Round ${roundNumber}`;

        prevBtn.disabled = index === 0;
        nextBtn.disabled = index === slides.length - 1;
    }

    prevBtn.addEventListener("click", () => {
        if (currentIndex > 0) {
            currentIndex--;
            showSlide(currentIndex);
        }
    });

    nextBtn.addEventListener("click", () => {
        if (currentIndex < slides.length - 1) {
            currentIndex++;
            showSlide(currentIndex);
        }
    });

    showSlide(currentIndex);
});

document.addEventListener("click", (e) => {

    if (e.target.id === "toggleMatchesBtn") {

        const container =
            document.querySelector(".league-round-slide.active .round-matches-container");

        if (!container) return;

        container.classList.toggle("hidden");

        e.target.textContent =
            container.classList.contains("hidden")
                ? "Show Matches"
                : "Hide Matches";
    }

    if (e.target.id === "toggleTableBtn") {

        const container =
            document.querySelector(".league-round-slide.active .round-table-container");

        if (!container) return;

        container.classList.toggle("hidden");

        e.target.textContent =
            container.classList.contains("hidden")
                ? "Show Table"
                : "Hide Table";
    }
});