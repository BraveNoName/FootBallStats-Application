document.addEventListener("DOMContentLoaded", function () {

    const input = document.getElementById("globalSearchInput");
    const box = document.getElementById("searchSuggestions");

    if (!input || !box) {
        return;
    }

    let results = [];
    let selectedIndex = -1;

    async function search() {
        const query = input.value.trim();

        if (query.length < 2) {
            box.innerHTML = "";
            box.classList.remove("show");
            results = [];
            selectedIndex = -1;
            return;
        }

        try {
            const response = await fetch("/api/search?q=" + encodeURIComponent(query));
            results = await response.json();

            box.innerHTML = "";
            selectedIndex = -1;

            if (results.length === 0) {
                box.innerHTML = `
                    <div class="search-suggestion empty">
                        No results found
                    </div>
                `;

                box.classList.add("show");
                return;
            }

            results.forEach(function (item, index) {
                const div = document.createElement("div");

                div.className = "search-suggestion";
                div.dataset.index = index;

                div.innerHTML = `
                    <div class="search-item-left">
                        <img src="${item.logo}"
                             alt="${item.name}"
                             class="search-logo">

                        <div class="search-info">
                            <strong>${item.name}</strong>
                            <span>${item.type}</span>
                        </div>
                    </div>
                `;

                div.addEventListener("mouseenter", function () {
                    selectedIndex = index;
                    updateSelection();
                });

                div.addEventListener("mousedown", function () {
                    window.location.href = item.url;
                });

                box.appendChild(div);
            });

            box.classList.add("show");

        } catch (error) {
            console.error("Search error:", error);
        }
    }

    function updateSelection() {
        const suggestions = box.querySelectorAll(".search-suggestion");

        suggestions.forEach(function (item) {
            item.classList.remove("active");
        });

        if (selectedIndex >= 0 && suggestions[selectedIndex]) {
            suggestions[selectedIndex].classList.add("active");
            suggestions[selectedIndex].scrollIntoView({
                block: "nearest"
            });
        }
    }

    input.addEventListener("input", search);

    input.addEventListener("keydown", function (e) {
        if (e.key === "ArrowDown") {
            e.preventDefault();

            if (results.length === 0) return;

            selectedIndex++;

            if (selectedIndex >= results.length) {
                selectedIndex = 0;
            }

            updateSelection();
        }

        else if (e.key === "ArrowUp") {
            e.preventDefault();

            if (results.length === 0) return;

            selectedIndex--;

            if (selectedIndex < 0) {
                selectedIndex = results.length - 1;
            }

            updateSelection();
        }

        else if (e.key === "Enter") {
            e.preventDefault();

            if (selectedIndex >= 0 && results[selectedIndex]) {
                window.location.href = results[selectedIndex].url;
            } else if (results.length > 0) {
                window.location.href = results[0].url;
            }
        }

        else if (e.key === "Escape") {
            box.classList.remove("show");
            selectedIndex = -1;
        }
    });

    document.addEventListener("click", function (e) {
        if (!e.target.closest(".search-box")) {
            box.classList.remove("show");
            selectedIndex = -1;
        }
    });
});