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