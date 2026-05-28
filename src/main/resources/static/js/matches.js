document.addEventListener("DOMContentLoaded", function () {

    const roundButtons = document.querySelectorAll(".round-btn");
    const roundViews = document.querySelectorAll(".round-view");

    function activateRound(roundNumber) {
        roundButtons.forEach(function (btn) {
            btn.classList.remove("active");

            if (btn.dataset.round === roundNumber) {
                btn.classList.add("active");
            }
        });

        roundViews.forEach(function (view) {
            view.classList.remove("active");

            if (view.dataset.round === roundNumber) {
                view.classList.add("active");
                resetSlider(view);
            }
        });
    }

    function resetSlider(roundView) {
        const cards = roundView.querySelectorAll(".match-card");
        const currentSlide = roundView.querySelector(".current-slide");
        const totalSlides = roundView.querySelector(".total-slides");

        cards.forEach(function (card) {
            card.classList.remove("active");
        });

        if (cards.length > 0) {
            cards[0].classList.add("active");
        }

        if (currentSlide) {
            currentSlide.textContent = "1";
        }

        if (totalSlides) {
            totalSlides.textContent = cards.length;
        }

        updateStatBars();
    }

    roundButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            activateRound(button.dataset.round);
        });
    });

    roundViews.forEach(function (roundView) {
        let index = 0;

        const cards = roundView.querySelectorAll(".match-card");
        const prevBtn = roundView.querySelector(".prev-btn");
        const nextBtn = roundView.querySelector(".next-btn");
        const currentSlide = roundView.querySelector(".current-slide");
        const totalSlides = roundView.querySelector(".total-slides");

        if (totalSlides) {
            totalSlides.textContent = cards.length;
        }

        function showCard(newIndex) {
            cards.forEach(function (card) {
                card.classList.remove("active");
            });

            index = newIndex;

            if (index < 0) {
                index = cards.length - 1;
            }

            if (index >= cards.length) {
                index = 0;
            }

            if (cards[index]) {
                cards[index].classList.add("active");
            }

            if (currentSlide) {
                currentSlide.textContent = index + 1;
            }

            updateStatBars();
        }

        if (prevBtn) {
            prevBtn.addEventListener("click", function () {
                showCard(index - 1);
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener("click", function () {
                showCard(index + 1);
            });
        }

        showCard(0);
    });

    function updateStatBars() {
        const statRows = document.querySelectorAll(".stat-row");

        statRows.forEach(function (row) {
            const home = Number(row.dataset.home);
            const away = Number(row.dataset.away);
            const total = home + away;

            const homeFill = row.querySelector(".home-fill");
            const awayFill = row.querySelector(".away-fill");

            if (!homeFill || !awayFill) {
                return;
            }

            if (total === 0) {
                homeFill.style.width = "50%";
                awayFill.style.width = "50%";
                return;
            }

            let homePercent = (home / total) * 100;
            let awayPercent = 100 - homePercent;

            if (home > 0 && homePercent < 4) {
                homePercent = 4;
                awayPercent = 96;
            }

            if (away > 0 && awayPercent < 4) {
                awayPercent = 4;
                homePercent = 96;
            }

            homeFill.style.width = homePercent + "%";
            awayFill.style.width = awayPercent + "%";
        });
    }

    const showFixturesBtn = document.getElementById("showFixturesBtn");
    const showTableBtn = document.getElementById("showTableBtn");
    const showPlayersBtn = document.getElementById("showPlayersBtn");

    const fixturesView = document.getElementById("fixturesView");
    const tableView = document.getElementById("tableView");
    const playersView = document.getElementById("playersView");

    if (
        showFixturesBtn
        && showTableBtn
        && showPlayersBtn
        && fixturesView
        && tableView
        && playersView
    ) {

        showFixturesBtn.addEventListener("click", function () {

            fixturesView.classList.remove("hidden");

            tableView.classList.remove("active");
            playersView.classList.remove("active");

            showFixturesBtn.classList.add("active");
            showTableBtn.classList.remove("active");
            showPlayersBtn.classList.remove("active");
        });

        showTableBtn.addEventListener("click", function () {

            fixturesView.classList.add("hidden");

            tableView.classList.add("active");
            playersView.classList.remove("active");

            showTableBtn.classList.add("active");
            showFixturesBtn.classList.remove("active");
            showPlayersBtn.classList.remove("active");
        });

        showPlayersBtn.addEventListener("click", function () {

            fixturesView.classList.add("hidden");

            tableView.classList.remove("active");
            playersView.classList.add("active");

            showPlayersBtn.classList.add("active");
            showFixturesBtn.classList.remove("active");
            showTableBtn.classList.remove("active");
        });
    }

    const generateForm = document.getElementById("generateForm");
    const leagueSelect = document.getElementById("leagueSelect");
    const leagueError = document.getElementById("leagueError");

   const seasonConfirmOverlay = document.getElementById("seasonConfirmOverlay");
   const cancelSeasonGenerateBtn = document.getElementById("cancelSeasonGenerateBtn");
   const confirmSeasonGenerateBtn = document.getElementById("confirmSeasonGenerateBtn");

   if (cancelSeasonGenerateBtn && seasonConfirmOverlay) {
       cancelSeasonGenerateBtn.addEventListener("click", function () {
           seasonConfirmOverlay.classList.remove("show");
       });
   }

   if (confirmSeasonGenerateBtn && generateForm) {
       confirmSeasonGenerateBtn.addEventListener("click", function () {
           generateForm.dataset.confirmed = "true";
           generateForm.submit();
       });
   }

   if (generateForm) {
       generateForm.addEventListener("submit", function (e) {
           if (!leagueSelect.value) {
               e.preventDefault();

               leagueError.classList.add("show");
               leagueSelect.style.border = "1px solid rgba(248,113,113,0.8)";
               return;
           }

           leagueError.classList.remove("show");
           leagueSelect.style.border = "1px solid rgba(255,255,255,0.14)";

           if (generateForm.dataset.seasonGenerated === "true"
               && generateForm.dataset.confirmed !== "true") {

               e.preventDefault();

               if (seasonConfirmOverlay) {
                   seasonConfirmOverlay.classList.add("show");
               }

               return;
           }
       });
   }

    const sortableHeaders =
        document.querySelectorAll(".sortable-header th[data-sort]");

    sortableHeaders.forEach(function (header, columnIndex) {
        let asc = false;

        header.addEventListener("click", function () {
            const table = header.closest("table");
            const tbody = table.querySelector("tbody");
            const rows = Array.from(tbody.querySelectorAll(".table-row"));

            sortableHeaders.forEach(function (h) {
                h.classList.remove("sort-asc", "sort-desc");
            });

            header.classList.add(asc ? "sort-asc" : "sort-desc");

            rows.sort(function (a, b) {
                const aCell = a.children[columnIndex];
                const bCell = b.children[columnIndex];

                const aValue = aCell.dataset.value || aCell.innerText.trim();
                const bValue = bCell.dataset.value || bCell.innerText.trim();

                const aNumber = parseFloat(aValue);
                const bNumber = parseFloat(bValue);

                if (!isNaN(aNumber) && !isNaN(bNumber)) {
                    return asc ? aNumber - bNumber : bNumber - aNumber;
                }

                return asc
                    ? aValue.localeCompare(bValue)
                    : bValue.localeCompare(aValue);
            });

            asc = !asc;

            rows.forEach(function (row) {
                tbody.appendChild(row);
            });
        });
    });

    const showMorePlayersBtn = document.getElementById("showMorePlayersBtn");

    if (showMorePlayersBtn) {
        showMorePlayersBtn.addEventListener("click", function () {
            document.querySelectorAll(".extra-player-row")
                .forEach(row => row.classList.remove("hidden-player-row"));

            showMorePlayersBtn.style.display = "none";
        });
    }

    updateStatBars();
});

async function toggleMatchEvents(matchId) {

    const eventsContainer = document.getElementById(`events-${matchId}`);
    const lineupsContainer = document.getElementById(`lineups-${matchId}`);
    const statsContainer = document.getElementById(`stats-${matchId}`);
    const button = document.querySelector(`[onclick="toggleMatchEvents(${matchId})"]`);

    if (!eventsContainer || !statsContainer || !button) {
        return;
    }

    if (eventsContainer.innerHTML.trim() !== "") {

        eventsContainer.innerHTML = "";

        statsContainer.style.display = "block";

        button.textContent = "Match events";

        return;
    }

    if (lineupsContainer) {
        lineupsContainer.innerHTML = "";
    }

    statsContainer.style.display = "none";

    button.textContent =
        "Click to return to match statistics";

    const response = await fetch(`/matches/${matchId}/events`);
    const events = await response.json();

    events.sort((a, b) => {

        if (a.eventMinute !== b.eventMinute) {
            return a.eventMinute - b.eventMinute;
        }

        return a.id - b.id;
    });

    let secondHalfAdded = false;

    let html = `
        <div class="events-timeline">

            <div class="half-divider">
                <span>1st half</span>
            </div>
    `;

    events.forEach(event => {

        if (!event.teamSide) {
            return;
        }

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
                    ${
                        event.teamSide === "HOME"
                            ? renderEventContent(event)
                            : ""
                    }
                </div>

                <div class="timeline-minute">
                    ${event.eventMinute}'
                </div>

                <div class="timeline-cell away-cell">
                    ${
                        event.teamSide === "AWAY"
                            ? renderEventContent(event)
                            : ""
                    }
                </div>

            </div>
        `;
    });

    html += `</div>`;

    eventsContainer.innerHTML = html;
}

async function toggleMatchLineups(matchId) {

    const lineupsContainer = document.getElementById(`lineups-${matchId}`);
    const eventsContainer = document.getElementById(`events-${matchId}`);
    const statsContainer = document.getElementById(`stats-${matchId}`);
    const button = document.querySelector(`[onclick="toggleMatchLineups(${matchId})"]`);

    if (!lineupsContainer || !statsContainer || !button) {
        return;
    }

    if (lineupsContainer.innerHTML.trim() !== "") {
        lineupsContainer.innerHTML = "";
        statsContainer.style.display = "block";
        button.textContent = "Lineups";
        return;
    }

    if (eventsContainer) {
        eventsContainer.innerHTML = "";
    }

    statsContainer.style.display = "none";
    button.textContent = "Click to return to match statistics";

    const response = await fetch(`/matches/${matchId}/lineups`);
    const lineups = await response.json();

    lineupsContainer.innerHTML = `

        <div class="lineups-section-title">
            Starting lineups
        </div>

        <div class="lineups-board">
            ${renderTeamLineup("", lineups.homeLineup, "home")}
            ${renderTeamLineup("", lineups.awayLineup, "away")}
        </div>

        <div class="lineups-section-title bench-title">
            Bench
        </div>

        <div class="lineups-board">
            ${renderTeamLineup("", lineups.homeBench, "home")}
            ${renderTeamLineup("", lineups.awayBench, "away")}
        </div>
    `;
}

function renderTeamLineup(title, players, side) {
    if (!players || players.length === 0) {
        return `
            <div class="lineup-column ${side}">
                ${title ? `<h4>${title}</h4>` : ""}
                <p class="empty-lineup">No generated players for this team.</p>
            </div>
        `;
    }

    return `
        <div class="lineup-column ${side}">
            <h4>${title}</h4>
            ${players.map(renderLineupPlayer).join("")}
        </div>
    `;
}

function renderLineupPlayer(player) {

    const number = player.shirtNumber ?? "-";
    const position = player.position ?? "";

    const image = player.imageUrl && player.imageUrl.trim() !== ""
        ? player.imageUrl
        : "/Images/player_placeholder.png";

    const countryCode = player.countryCode
        ? player.countryCode.toLowerCase()
        : "";

    const flag = countryCode
        ? `<span class="fi fi-${countryCode} lineup-flag"></span>`
        : "";

    const eventIcons = player.events && player.events.length > 0
        ? player.events.map(event => `
            <img class="lineup-event-icon"
                 src="${event.iconUrl}"
                 title="${formatLineupEventTitle(event)}"
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

                    <strong>
                        ${player.name}
                    </strong>

                    <span>
                        ${position}
                    </span>

                </div>

            </div>

            <div class="lineup-player-events">
                ${eventIcons}
            </div>

        </div>
    `;
}

function formatLineupEventTitle(event) {
    const names = {
        GOAL: "Goal",
        PENALTY_GOAL: "Penalty scored",
        PENALTY_MISSED: "Penalty missed",
        OWN_GOAL: "Own goal",
        YELLOW_CARD: "Yellow card",
        RED_CARD: "Red card",
        SECOND_YELLOW_RED_CARD: "Second yellow card",
        INJURY: "Injury",
        SUBSTITUTION: "Substitution"
    };

    const eventName = names[event.eventType] ?? event.eventType;
    const minute = event.minute ? ` ${event.minute}'` : "";

    return eventName + minute;
}

function renderEventContent(event) {

    return `
        <div class="timeline-event-content">

            <img class="event-icon"
                 src="${event.iconUrl}"
                 alt="${event.eventType}">

            <div class="event-text">

                <strong>
                    ${event.playerName ?? ""}
                </strong>

                <small>
                    ${buildEventDescription(event)}
                </small>

            </div>

        </div>
    `;
}

function formatRelatedEvent(event) {

    if (event.eventType === "GOAL") {
        return "Assist: " + event.relatedPlayerName;
    }

    if (event.eventType === "SUBSTITUTION") {
        return "For: " + event.relatedPlayerName;
    }

    return event.relatedPlayerName;
}

function buildEventDescription(event) {

    if (event.eventType === "GOAL" && event.relatedPlayerName) {
        return "Assist: " + event.relatedPlayerName;
    }

    if (event.eventType === "SUBSTITUTION") {
        return "In: " + event.playerName
            + " / Out: " + event.relatedPlayerName;
    }

    return event.description ?? "";
}