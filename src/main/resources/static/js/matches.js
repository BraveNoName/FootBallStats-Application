document.addEventListener("DOMContentLoaded", function () {

    const selectedLeagueInput = document.getElementById("selectedLeague");
    const leagueChoices = document.querySelectorAll(".league-choice");

    if (selectedLeagueInput) {
        const currentLeague = selectedLeagueInput.value;

        leagueChoices.forEach(function (choice) {
            if (choice.dataset.league === currentLeague) {
                choice.classList.add("active");
            }

            choice.addEventListener("click", function () {
                const league = choice.dataset.league;
                window.location.href = "/matches?league=" + league;
            });
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

            if (!homeFill || !awayFill || total === 0) {
                return;
            }

            const homePercent = (home / total) * 100;
            const awayPercent = 100 - homePercent;

            homeFill.style.width = homePercent + "%";
            awayFill.style.width = awayPercent + "%";
        });
    }

    updateStatBars();
});


