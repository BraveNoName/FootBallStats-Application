const generateBtn = document.getElementById("generateSquadsBtn");
const overlay = document.getElementById("regenerateOverlay");

if (overlay) {
    overlay.className = "regenerate-overlay";
    overlay.innerHTML = `
        <div class="regenerate-box">
            <h3>Regenerate squads</h3>
            <p>Players have already been generated for all clubs. Do you want to overwrite them with newly generated squads?</p>
            <div class="regenerate-actions">
                <button type="button" id="confirmRegenerateBtn">Regenerate</button>
                <button type="button" id="closeRegenerateBtn">Cancel</button>
            </div>
        </div>
    `;
}

const confirmBtn = document.getElementById("confirmRegenerateBtn");
const closeBtn = document.getElementById("closeRegenerateBtn");

if (generateBtn && overlay) {
    generateBtn.addEventListener("click", (e) => {
        e.preventDefault();

        const title = overlay.querySelector("h3");
        const text = overlay.querySelector("p");

        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerText = "Regenerate";
            confirmBtn.style.display = "inline-block";
            confirmBtn.style.background = "";
        }

        if (closeBtn) {
            closeBtn.style.display = "inline-block";
        }

        if (generateBtn.dataset.generated !== "true") {
            overlay.classList.add("show");

            title.innerText = "Generate squads";
            text.innerText = "Generating squads for all clubs...";

            confirmBtn.style.display = "none";
            closeBtn.style.display = "none";

            let dots = 0;

            const interval = setInterval(() => {
                dots = (dots + 1) % 4;
                text.innerText = "Generating squads for all clubs" + ".".repeat(dots);
            }, 450);

            setTimeout(() => {
                clearInterval(interval);
                text.innerText = "Squads generated successfully ✓";

                setTimeout(() => {
                    window.location.href = "/players/generated/generate-for-real-clubs";
                }, 1800);
            }, 4200);

            return;
        }

        title.innerText = "Regenerate squads";
        text.innerText = "Players have already been generated for all clubs. Do you want to overwrite them with newly generated?";

        overlay.classList.add("show");
    });
}

if (confirmBtn) {
    confirmBtn.addEventListener("click", () => {
        const text = overlay.querySelector("p");

        confirmBtn.disabled = true;
        closeBtn.style.display = "none";

        let dots = 0;
        text.innerText = "Generating squads";

        const generatingInterval = setInterval(() => {
            dots = (dots + 1) % 4;
            text.innerText = "Generating squads" + ".".repeat(dots);
        }, 450);

        setTimeout(() => {
            clearInterval(generatingInterval);

            text.innerText = "Squads generated successfully ✓";
            confirmBtn.innerText = "Done";
            confirmBtn.style.background = "linear-gradient(135deg, #16a34a 0%, #15803d 100%)";

            setTimeout(() => {
                window.location.href = "/players/generated/regenerate";
            }, 5000);
        }, 10000);
    });
}

if (closeBtn) {
    closeBtn.addEventListener("click", () => {
        overlay.classList.remove("show");
    });
}

if (overlay) {
    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) {
            overlay.classList.remove("show");
        }
    });
}