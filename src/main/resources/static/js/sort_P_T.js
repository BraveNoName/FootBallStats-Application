const getCellValue = (row, index) => {
    return row.children[index].innerText.trim();
};

const parseValue = (value, type) => {

    if (type === "number") {
        return parseFloat(value.replace(/[^\d.-]/g, "")) || 0;
    }

    if (type === "money") {

        value = value.toLowerCase();

        let multiplier = 1;

        if (value.includes("bn")) multiplier = 1000000000;
        else if (value.includes("m")) multiplier = 1000000;
        else if (value.includes("k")) multiplier = 1000;

        const number =
            parseFloat(value.replace(/[^\d.]/g, "")) || 0;

        return number * multiplier;
    }

    if (type === "date") {

        const date = new Date(value);

        return isNaN(date.getTime())
            ? 0
            : date.getTime();
    }

    return value.toLowerCase();
};

document.querySelectorAll(".sortable").forEach((header) => {

    let asc = true;

    header.addEventListener("click", () => {

        const table =
            header.closest("table");

        const tbody =
            table.querySelector("tbody");

        const headers =
            Array.from(header.parentElement.children);

        const index =
            headers.indexOf(header);

        const type =
            header.dataset.type;

        const rows =
            Array.from(tbody.querySelectorAll("tr"));

        rows.sort((a, b) => {

            const aValue =
                parseValue(getCellValue(a, index), type);

            const bValue =
                parseValue(getCellValue(b, index), type);

            if (aValue > bValue) return asc ? 1 : -1;
            if (aValue < bValue) return asc ? -1 : 1;

            return 0;
        });

        asc = !asc;

        tbody.innerHTML = "";

        rows.forEach(row => tbody.appendChild(row));

        table.querySelectorAll(
            ".fa-sort, .fa-sort-up, .fa-sort-down"
        ).forEach(icon => {

            icon.className =
                "fa-solid fa-sort";
        });

        const currentIcon =
            header.querySelector("i");

        if (currentIcon) {

            currentIcon.className =
                asc
                    ? "fa-solid fa-sort-down"
                    : "fa-solid fa-sort-up";
        }
    });
});

function validateGenerateForm() {

    const input =
        document.querySelector(".generate-input");

    if (!input.value || Number(input.value) < 1) {

        alert(input.dataset.alert);

        return false;
    }

    return true;
}

const leagueSelect =
    document.querySelector('select[name="league"]');

const clubSelect =
    document.querySelector('select[name="club"]');

if (leagueSelect && clubSelect) {

    const originalOptions =
        Array.from(clubSelect.querySelectorAll("option"));

    function filterClubs() {

        const selectedLeague =
               leagueSelect.value;

           clubSelect.disabled =
               selectedLeague === "";

           clubSelect.title =
               selectedLeague === ""
                   ? "Select a league first"
                   : "";

           clubSelect.innerHTML = "";

        originalOptions.forEach(option => {

            const optionLeague =
                option.getAttribute("data-league");

            if (
                option.value === "" ||
                selectedLeague === "" ||
                optionLeague === selectedLeague
            ) {
                clubSelect.appendChild(
                    option.cloneNode(true)
                );
            }
        });
    }

    filterClubs();

    leagueSelect.addEventListener(
        "change",
        filterClubs
    );
}

const playerSearchInput =
    document.getElementById("playerSearchInput");

const playerSuggestions =
    document.getElementById("playerSuggestions");

if (playerSearchInput && playerSuggestions) {

    playerSearchInput.addEventListener("input", () => {

        const query =
            playerSearchInput.value
                .trim()
                .toLowerCase();

        playerSuggestions.innerHTML = "";

        if (
            query.length < 1 ||
            !window.playersSearchData
        ) {

            playerSuggestions.classList.remove("show");

            return;
        }

        const filtered =
            window.playersSearchData

                .filter(player => {

                    const fullName =
                        player.fullName ||
                        player.name ||
                        `${player.firstName || ""} ${player.lastName || ""}`.trim();

                    return fullName
                        .toLowerCase()
                        .includes(query);
                })

                .slice(0, 8);

        filtered.forEach(player => {

            const fullName =
                player.fullName ||
                player.name ||
                `${player.firstName || ""} ${player.lastName || ""}`.trim();

            const club =
                player.currentClub ||
                player.teamName ||
                player.clubSlug ||
                "";

            const image =
                player.imageUrl ||
                player.photo ||
                "/Images/default-player.png";

            const item =
                document.createElement("div");

            item.className =
                "search-suggestion";

            item.innerHTML = `
                <div class="search-item-left">

                    <img src="${image}"
                         class="search-logo"
                         onerror="this.src='/Images/default-player.png'">

                    <div class="search-info">

                        <strong>${fullName}</strong>

                        <span>${club}</span>

                    </div>

                </div>
            `;

            item.addEventListener("click", () => {

                playerSearchInput.value =
                    fullName;

                playerSuggestions.classList.remove("show");
            });

            playerSuggestions.appendChild(item);
        });

        playerSuggestions.classList.toggle(
            "show",
            filtered.length > 0
        );
    });

    document.addEventListener("click", e => {

        if (
            !e.target.closest(".player-search-wrapper")
        ) {
            playerSuggestions.classList.remove("show");
        }
    });
}
