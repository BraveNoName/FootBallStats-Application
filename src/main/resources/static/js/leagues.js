const filterButtons = document.querySelectorAll(".league-filter");
const leagueCards = document.querySelectorAll(".league-card");
const leagueGrid = document.querySelector(".leagues-grid");
const leagueSearch = document.querySelector("#leagueSearch");
const noLeaguesMessage = document.querySelector("#noLeaguesMessage");

let currentFilter = "all";

function applyLeagueFilters() {
    const searchValue = leagueSearch.value.toLowerCase().trim();
    let visibleCount = 0;

    leagueCards.forEach(card => {
        const category = card.dataset.category;
        const leagueName = card.innerText.toLowerCase();

        const matchesFilter = currentFilter === "all" || category === currentFilter;
        const matchesSearch = leagueName.includes(searchValue);

        if (matchesFilter && matchesSearch) {
            card.style.display = "flex";
            visibleCount++;
        } else {
            card.style.display = "none";
        }
    });

    if (currentFilter === "top") {
        leagueGrid.classList.add("top-mode");
    } else {
        leagueGrid.classList.remove("top-mode");
    }

    noLeaguesMessage.style.display = visibleCount === 0 ? "block" : "none";
}

filterButtons.forEach(button => {
    button.addEventListener("click", () => {
        currentFilter = button.dataset.filter;

        filterButtons.forEach(btn => btn.classList.remove("active"));
        button.classList.add("active");

        applyLeagueFilters();
    });
});

leagueSearch.addEventListener("input", applyLeagueFilters);