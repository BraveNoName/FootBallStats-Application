const table = document.querySelector("#squadTable");

if (table) {
    const headers = table.querySelectorAll("th[data-sort]");
    let sortDirection = {};

    headers.forEach(header => {
        header.addEventListener("click", () => {
            const sortKey = header.dataset.sort;
            const tbody = table.querySelector("tbody");
            const rows = Array.from(tbody.querySelectorAll("tr"));

            sortDirection[sortKey] = !sortDirection[sortKey];

            rows.sort((a, b) => {
                const aValue = Number(a.dataset[sortKey] || 0);
                const bValue = Number(b.dataset[sortKey] || 0);

                return sortDirection[sortKey]
                    ? aValue - bValue
                    : bValue - aValue;
            });

            rows.forEach(row => tbody.appendChild(row));
        });
    });
}