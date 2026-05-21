document.querySelectorAll(".league-filter").forEach(button => {
    button.addEventListener("click", () => {
        const filter = button.dataset.filter;
        const grid = document.querySelector(".leagues-grid");

        document.querySelectorAll(".league-filter").forEach(btn => {
            btn.classList.remove("active");
        });

        button.classList.add("active");

        if (filter === "top") {
            grid.classList.add("top-mode");
        } else {
            grid.classList.remove("top-mode");
        }

        document.querySelectorAll(".league-card").forEach(card => {
            const category = card.dataset.category;

            if (filter === "all" || category === filter) {
                card.style.display = "flex";
            } else {
                card.style.display = "none";
            }
        });
    });
});