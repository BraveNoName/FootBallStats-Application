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

    updateEuropeanStatBars();
});

document.addEventListener("DOMContentLoaded", () => {
    const slides = document.querySelectorAll(".league-round-slide");
    const prevBtn = document.getElementById("prevRoundBtn");
    const nextBtn = document.getElementById("nextRoundBtn");
    const title = document.getElementById("roundSliderTitle");

    if (!slides.length || !prevBtn || !nextBtn || !title) {
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const requestedRound = params.get("round");

    let currentIndex = 0;

    if (requestedRound) {
        const foundIndex = Array.from(slides).findIndex(slide =>
            slide.dataset.round === requestedRound
        );

        if (foundIndex !== -1) {
            currentIndex = foundIndex;
        }
    }

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
    if (e.target.classList.contains("toggle-matches-btn")) {
        const activeSlide = e.target.closest(".league-round-slide");
        const container = activeSlide?.querySelector(".round-matches-container");

        if (!container) return;

        container.classList.toggle("hidden");

        e.target.textContent =
            container.classList.contains("hidden")
                ? "Show Matches"
                : "Hide Matches";
    }

    if (e.target.classList.contains("toggle-table-btn")) {
        const activeSlide = e.target.closest(".league-round-slide");
        const container = activeSlide?.querySelector(".round-table-container");

        if (!container) return;

        container.classList.toggle("hidden");

        e.target.textContent =
            container.classList.contains("hidden")
                ? "Show Table"
                : "Hide Table";
    }
});

function updateEuropeanStatBars() {
    const statRows = document.querySelectorAll(".stat-row");

    statRows.forEach(row => {
        const home = Number(row.dataset.home);
        const away = Number(row.dataset.away);
        const total = home + away;

        const homeFill = row.querySelector(".home-fill");
        const awayFill = row.querySelector(".away-fill");

        if (!homeFill || !awayFill) return;

        if (total === 0 || isNaN(total)) {
            homeFill.style.width = "50%";
            awayFill.style.width = "50%";
            return;
        }

        homeFill.style.width = ((home / total) * 100) + "%";
        awayFill.style.width = ((away / total) * 100) + "%";
    });
}

async function toggleEuropeanMatchEvents(matchId) {
    const eventsContainer = document.getElementById(`europe-events-${matchId}`);
    const lineupsContainer = document.getElementById(`europe-lineups-${matchId}`);
    const statsContainer = document.getElementById(`europe-stats-${matchId}`);
    const button = document.querySelector(`[onclick="toggleEuropeanMatchEvents(${matchId})"]`);
    const lineupsButton = document.querySelector(`[onclick="toggleEuropeanMatchLineups(${matchId})"]`);

    if (!eventsContainer || !statsContainer || !button) return;

    if (eventsContainer.innerHTML.trim() !== "") {
        eventsContainer.innerHTML = "";
        statsContainer.style.display = "block";
        button.textContent = "Match events";
        updateEuropeanStatBars();
        return;
    }

    if (lineupsContainer) {
        lineupsContainer.innerHTML = "";
    }

    if (lineupsButton) {
        lineupsButton.textContent = "Lineups";
    }

    statsContainer.style.display = "none";
    button.textContent = "Click to return to match statistics";

    const response = await fetch(`/europe/match/${matchId}/events`);
    const events = await response.json();

    events.sort((a, b) => {
        if (a.eventMinute !== b.eventMinute) {
            return a.eventMinute - b.eventMinute;
        }

        return a.id - b.id;
    });

    let html = `
        <div class="events-timeline">

            <div class="half-divider">
                <span>1st half</span>
            </div>
    `;

    let secondHalfAdded = false;

    events.forEach(event => {
        if (!event.teamSide) return;

        if (!secondHalfAdded && event.eventMinute >= 46) {
            html += `
                <div class="half-divider">
                    <span>2nd half</span>
                </div>
            `;

            secondHalfAdded = true;
        }

        html += `
            <div class="timeline-row">

                <div class="timeline-cell home-cell">
                    ${event.teamSide === "HOME" ? renderEuropeanEvent(event) : ""}
                </div>

                <div class="timeline-minute">
                    ${event.eventMinute}'
                </div>

                <div class="timeline-cell away-cell">
                    ${event.teamSide === "AWAY" ? renderEuropeanEvent(event) : ""}
                </div>

            </div>
        `;
    });

    html += `</div>`;

    eventsContainer.innerHTML = html;
}

async function toggleEuropeanMatchLineups(matchId) {
    const lineupsContainer = document.getElementById(`europe-lineups-${matchId}`);
    const eventsContainer = document.getElementById(`europe-events-${matchId}`);
    const statsContainer = document.getElementById(`europe-stats-${matchId}`);
    const button = document.querySelector(`[onclick="toggleEuropeanMatchLineups(${matchId})"]`);
    const eventsButton = document.querySelector(`[onclick="toggleEuropeanMatchEvents(${matchId})"]`);

    if (!lineupsContainer || !statsContainer || !button) return;

    if (lineupsContainer.innerHTML.trim() !== "") {
        lineupsContainer.innerHTML = "";
        statsContainer.style.display = "block";
        button.textContent = "Lineups";
        updateEuropeanStatBars();
        return;
    }

    if (eventsContainer) {
        eventsContainer.innerHTML = "";
    }

    if (eventsButton) {
        eventsButton.textContent = "Match events";
    }

    statsContainer.style.display = "none";
    button.textContent = "Click to return to match statistics";

    const response = await fetch(`/europe/match/${matchId}/lineups`);
    const lineups = await response.json();

    lineupsContainer.innerHTML = `
        <div class="lineups-section-title">
            Starting lineups
        </div>

        <div class="lineups-board">
            ${renderEuropeanTeamLineup(lineups.homeLineup, "home")}
            ${renderEuropeanTeamLineup(lineups.awayLineup, "away")}
        </div>

        <div class="lineups-section-title bench-title">
            Bench
        </div>

        <div class="lineups-board">
            ${renderEuropeanTeamLineup(lineups.homeBench, "home")}
            ${renderEuropeanTeamLineup(lineups.awayBench, "away")}
        </div>
    `;
}

function renderEuropeanTeamLineup(players, side) {
    if (!players || players.length === 0) {
        return `
            <div class="lineup-column ${side}">
                <p class="empty-lineup">No generated players for this team.</p>
            </div>
        `;
    }

    return `
        <div class="lineup-column ${side}">
            ${players.map(renderEuropeanLineupPlayer).join("")}
        </div>
    `;
}

function renderEuropeanLineupPlayer(player) {
    const number = player.shirtNumber ?? "-";
    const position = player.position ?? "";
    const countryCode = player.countryCode ? player.countryCode.toLowerCase() : "";
    const flag = countryCode ? `<span class="fi fi-${countryCode} lineup-flag"></span>` : "";

    const eventIcons = player.events && player.events.length > 0
        ? player.events.map(event => `
            <img class="lineup-event-icon"
                 src="${event.iconUrl}"
                 title="${formatEuropeanLineupEventTitle(event)}"
                 alt="${event.eventType}">
        `).join("")
        : "";

    return `
        <div class="lineup-player">

            <div class="lineup-player-left">

                <div class="lineup-number">
                    ${number}
                </div>

                ${flag}

                <div class="lineup-player-info">
                    <strong>${player.name}</strong>
                    <span>${position}</span>
                </div>

            </div>

            <div class="lineup-player-events">
                ${eventIcons}
            </div>

        </div>
    `;
}

function renderEuropeanEvent(event) {
    return `
        <div class="timeline-event-content">

            <img class="event-icon"
                 src="${event.iconUrl}"
                 alt="${event.eventType}">

            <div class="event-text">
                <strong>${event.playerName ?? ""}</strong>
                <small>${event.description ?? ""}</small>
            </div>

        </div>
    `;
}

function formatEuropeanLineupEventTitle(event) {
    const names = {
        GOAL: "Goal",
        ASSIST: "Assist",
        YELLOW_CARD: "Yellow card",
        RED_CARD: "Red card"
    };

    const eventName = names[event.eventType] ?? event.eventType;
    const minute = event.minute ? ` ${event.minute}'` : "";

    return eventName + minute;
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("generateEuropeanForm");
    const overlay = document.getElementById("europeanGenerateOverlay");
    const title = document.getElementById("europeanGenerateTitle");
    const message = document.getElementById("europeanGenerateMessage");
    const cancelBtn = document.getElementById("cancelEuropeanGenerateBtn");
    const confirmBtn = document.getElementById("confirmEuropeanGenerateBtn");

    const page = document.querySelector(".european-page");
    const competition = page?.dataset.competition || "";

    if (!form || !overlay || !title || !message || !cancelBtn || !confirmBtn) {
        return;
    }

    form.addEventListener("submit", async (e) => {
        if (form.dataset.confirmed === "true") {
            return;
        }

        e.preventDefault();

        const response = await fetch(`/europe/${competition}/generation-status`);
        const status = await response.json();

        if (status.needsDomesticRegeneration) {
            title.textContent = "Domestic leagues need regeneration";

            message.textContent =
                `Missing domestic leagues: ${status.missingLeaguesCount}. ` +
                "The system will generate new domestic seasons first, then create this European competition.";

            confirmBtn.style.display = "inline-block";
            confirmBtn.textContent = "Generate all";

        } else if (status.competitionGenerated) {
            title.textContent = "European competition already generated";

            message.textContent =
                "Domestic leagues are ready. Generating again will create a new European competition season.";

            confirmBtn.style.display = "inline-block";
            confirmBtn.textContent = "Generate new season";

        } else {
            title.textContent = "Generate European competition";

            message.textContent =
                "Domestic leagues are ready. The system will generate this European competition.";

            confirmBtn.style.display = "inline-block";
            confirmBtn.textContent = "Generate";
        }

        overlay.classList.add("show");
    });

    cancelBtn.addEventListener("click", () => {
        overlay.classList.remove("show");
    });

    confirmBtn.addEventListener("click", () => {
        form.dataset.confirmed = "true";
        form.submit();
    });
});