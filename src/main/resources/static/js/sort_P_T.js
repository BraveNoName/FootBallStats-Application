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

            const number = parseFloat(value.replace(/[^\d.]/g, "")) || 0;
            return number * multiplier;
        }

        if (type === "date") {
            const date = new Date(value);
            return isNaN(date.getTime()) ? 0 : date.getTime();
        }

        return value.toLowerCase();
    };

    document.querySelectorAll(".sortable").forEach((header) => {
        let asc = true;

        header.addEventListener("click", () => {
            const table = header.closest("table");
            const tbody = table.querySelector("tbody");
            const headers = Array.from(header.parentElement.children);
            const index = headers.indexOf(header);
            const type = header.dataset.type;
            const rows = Array.from(tbody.querySelectorAll("tr"));

            rows.sort((a, b) => {
                const aValue = parseValue(getCellValue(a, index), type);
                const bValue = parseValue(getCellValue(b, index), type);

                if (aValue > bValue) return asc ? 1 : -1;
                if (aValue < bValue) return asc ? -1 : 1;
                return 0;
            });

            asc = !asc;

            tbody.innerHTML = "";
            rows.forEach(row => tbody.appendChild(row));
            
            // Update sort icons
            table.querySelectorAll(".fa-sort, .fa-sort-up, .fa-sort-down").forEach(icon => {
                icon.className = "fa-solid fa-sort";
            });
            const currentIcon = header.querySelector("i");
            if (currentIcon) {
                currentIcon.className = asc ? "fa-solid fa-sort-down" : "fa-solid fa-sort-up";
            }
        });
    });

    // Check if numbers of generating player was input
    function validateGenerateForm() {
        const input = document.querySelector(".generate-input");
        if (!input.value || Number(input.value) < 1) {
            alert(input.dataset.alert);
            return false;
        }
        return true;
    }

