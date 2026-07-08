document.addEventListener("DOMContentLoaded", function () {

    const toggleCompetitionHubBtn = document.getElementById("toggleCompetitionHubBtn");
    const showDomesticPanel = document.getElementById("showDomesticPanel");
    const showEuropeanPanel = document.getElementById("showEuropeanPanel");
    const domesticControls = document.getElementById("domesticControls");
    const europeanControls = document.getElementById("europeanControls");
    const emptyMatchesInfo = document.getElementById("emptyMatchesInfo");
    const competitionHub = document.querySelector(".competition-hub");
    const seasonPanel = document.getElementById("seasonPanel");

    if (window.location.search.includes("league=")) {

        if (competitionHub) {
            competitionHub.style.display = "none";
        }

        if (toggleCompetitionHubBtn) {
            toggleCompetitionHubBtn.classList.remove("hidden-controls");
        }

        if (domesticControls) {
            domesticControls.classList.add("show");
        }
    }

    if (showDomesticPanel && showEuropeanPanel && domesticControls && europeanControls) {

        showDomesticPanel.addEventListener("click", function () {

            if (competitionHub) {
                competitionHub.style.display = "none";
            }

            domesticControls.classList.add("show");
            europeanControls.classList.remove("show");

            if (emptyMatchesInfo) {
                emptyMatchesInfo.classList.add("show");
            }

            if (toggleCompetitionHubBtn) {
                toggleCompetitionHubBtn.classList.remove("hidden-controls");
            }
        });

    }

    if (toggleCompetitionHubBtn) {

        toggleCompetitionHubBtn.addEventListener("click", function () {
            window.location.href = "/matches";
        });
    }

    const leagueData = {
        england: {
            name: "Premier League",
            logo: "/Images/leagues/England - Premier League/premierleague.png"
        },
        spain: {
            name: "LaLiga EA Sports",
            logo: "/Images/leagues/Spain - LaLiga/laliga.png"
        },
        italy: {
            name: "Serie A",
            logo: "/Images/leagues/Italy - Serie A/seriea.png"
        },
        germany: {
            name: "Bundesliga",
            logo: "/Images/leagues/Germany - Bundesliga/bundesliga.png"
        },
        france: {
            name: "Ligue 1",
            logo: "/Images/leagues/France - Ligue 1/ligue1.png"
        },
        poland: {
            name: "Ekstraklasa",
            logo: "/Images/leagues/Poland - PKO BP Ekstraklasa/pkobpekstraklasa.png"
        },
        portugal: {
            name: "Liga Portugal",
            logo: "/Images/leagues/Portugal - Liga Portugal/ligaportugal.png"
        },
        netherlands: {
            name: "Eredivisie",
            logo: "/Images/leagues/Netherlands - Eredivisie/eredivisie.png"
        },
        belgium: {
            name: "Jupiler Pro League",
            logo: "/Images/leagues/Belgium - Jupiler Pro League/proleague.png"
        },
        austria: {
            name: "Austrian Bundesliga",
            logo: "/Images/leagues/Austria - Bundesliga/austriabundesliga.png"
        },
        denmark: {
            name: "Danish Superliga",
            logo: "/Images/leagues/Denmark - Superliga/superliga.png"
        },
        norway: {
            name: "Eliteserien",
            logo: "/Images/leagues/Norway - Eliteserien/eliteserien.png"
        },
        scotland: {
            name: "Scottish Premiership",
            logo: "/Images/leagues/Scotland - Scottish Premiership/scottishpremiership.png"
        },
        sweden: {
            name: "Allsvenskan",
            logo: "/Images/leagues/Sweden - Allsvenskan/allsvenskan.png"
        },
        switzerland: {
            name: "Swiss Super League",
            logo: "/Images/leagues/Switzerland - Super League/superleague.png"
        },
        ukraine: {
            name: "Ukrainian Premier League",
            logo: "/Images/leagues/Ukraine - Premier Liga/premierliga.png"
        },
        serbia: {
            name: "Serbian SuperLiga",
            logo: "/Images/leagues/Serbia - Super liga Srbije/superligaserbije.png"
        },
        turkiye: {
            name: "Süper Lig",
            logo: "/Images/leagues/Turkiye - Super Lig/superlig.png"
        },
        turkey: {
            name: "Süper Lig",
            logo: "/Images/leagues/Turkiye - Super Lig/superlig.png"
        },
        greece: {
            name: "Super League Greece",
            logo: "/Images/leagues/Greece - Super League 1/superleague1.png"
        },
        czech: {
            name: "Czech First League",
            logo: "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png"
        },
        "czech-republic": {
            name: "Czech First League",
            logo: "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png"
        },
        croatia: {
            name: "HNL",
            logo: "/Images/leagues/Croatia - SuperSport HNL/supersporthnl.png"
        },
        bulgaria: {
            name: "efbet Liga",
            logo: "/Images/leagues/Bulgaria - efbet Liga/efbetliga.png"
        },
        israel: {
            name: "Ligat ha Al",
            logo: "/Images/leagues/Israel - Ligat ha Al/ligathaai.png"
        },
        romania: {
            name: "SuperLiga",
            logo: "/Images/leagues/Romania - SuperLiga/romaniasuperliga.png"
        },
        russia: {
            name: "Premier Liga",
            logo: "/Images/leagues/Russia - Premier Liga/russiapremierliga.png"
        },
        hungary: {
            name: "Nemezeti Bajnoksag",
            logo: "/Images/leagues/Hungary - Nemezeti Bajnoksag/NemzetiBanjnoksag.png"
        },
        moldova: {
            name: "Superliga",
            logo: "/Images/leagues/Moldova - Superliga/superliga.png"
        },
        "republic-of-ireland": {
            name: "Ireland Premier Division",
            logo: "/Images/leagues/Republic of Ireland - Ireland Premier Division/irelandpremierdivision.png"
        },
    };

    const leagueSelect = document.getElementById("leagueSelect");
    const leagueDropdownBtn = document.getElementById("leagueDropdownBtn");
    const leagueDropdownMenu = document.getElementById("leagueDropdownMenu");
    const selectedLeagueLogo = document.getElementById("selectedLeagueLogo");
    const selectedLeagueName = document.getElementById("selectedLeagueName");

    if (leagueSelect && leagueDropdownBtn && leagueDropdownMenu) {

        function setSelectedLeague(slug) {
            const data = leagueData[slug];

            if (!data) {
                selectedLeagueName.textContent = "Select league";
                selectedLeagueLogo.src = "/Images/Favicon.png";
                return;
            }

            selectedLeagueName.textContent = data.name;
            selectedLeagueLogo.src = data.logo;
        }

        leagueDropdownMenu.innerHTML = "";

        Array.from(leagueSelect.options).forEach(function (option) {
            const slug = option.value;

            if (!slug || !leagueData[slug]) {
                return;
            }

            option.textContent = leagueData[slug].name;

            const div = document.createElement("div");
            div.className = "league-dropdown-option";

            div.innerHTML = `
                <img src="${leagueData[slug].logo}" alt="">
                <span>${leagueData[slug].name}</span>
            `;

            div.addEventListener("click", function () {
                leagueSelect.value = slug;

                if (competitionHub) {
                    competitionHub.style.display = "none";
                }

                if (seasonPanel) {
                    seasonPanel.style.display = "block";
                }

                window.location.href = "/matches?league=" + slug;
            });

            leagueDropdownMenu.appendChild(div);
        });

        setSelectedLeague(leagueSelect.value);

        leagueDropdownBtn.addEventListener("click", function () {
            leagueDropdownMenu.classList.toggle("show");
        });

        document.addEventListener("click", function (event) {
            if (!event.target.closest(".league-select-wrapper")) {
                leagueDropdownMenu.classList.remove("show");
            }
        });
    }

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
    const seasonConfirmOverlay = document.getElementById("seasonConfirmOverlay");
    const cancelSeasonGenerateBtn = document.getElementById("cancelSeasonGenerateBtn");
    const confirmSeasonGenerateBtn = document.getElementById("confirmSeasonGenerateBtn");

    if (cancelSeasonGenerateBtn && seasonConfirmOverlay) {
        cancelSeasonGenerateBtn.addEventListener("click", function () {
            seasonConfirmOverlay.classList.remove("show");
        });
    }

    const seasonGenerateTitle =
        document.getElementById("seasonGenerateTitle");

    const seasonGenerateText =
        document.getElementById("seasonGenerateText");

    const seasonGenerateActions =
        document.getElementById("seasonGenerateActions");

    if (confirmSeasonGenerateBtn && generateForm) {

        confirmSeasonGenerateBtn.addEventListener("click", function () {

            generateForm.dataset.confirmed = "true";

            if (seasonGenerateTitle) {
                seasonGenerateTitle.textContent =
                    "Generating domestic leagues...";
            }

            if (seasonGenerateText) {
                seasonGenerateText.innerHTML =
                    `
                    <i class="fa-solid fa-spinner fa-spin"></i><br><br>
                    Please wait while all domestic leagues are being generated.
                    `;
            }

            if (seasonGenerateActions) {
                seasonGenerateActions.style.display = "none";
            }

            generateForm.submit();
        });
    }

    if (generateForm && leagueSelect) {
        generateForm.addEventListener("submit", function (e) {

            if (leagueDropdownBtn) {
                leagueDropdownBtn.style.border = "1px solid rgba(255,255,255,0.14)";
            }

            if (
                generateForm.dataset.seasonGenerated === "true"
                && generateForm.dataset.confirmed !== "true"
            ) {
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
    button.textContent = "Click to return to match statistics";

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