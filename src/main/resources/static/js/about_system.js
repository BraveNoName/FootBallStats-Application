document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll('a[href^="#"]').forEach(link => {
        link.addEventListener("click", event => {

            const target = document.querySelector(
                link.getAttribute("href")
            );

            if (!target) {
                return;
            }

            event.preventDefault();

            target.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        });
    });

    const observer = new IntersectionObserver(entries => {

        entries.forEach(entry => {

            if (entry.isIntersecting) {
                entry.target.classList.add("show");
            }

        });

    }, {
        threshold: 0.15
    });

    document.querySelectorAll(
        ".about-section, .about-metrics article"
    ).forEach(el => observer.observe(el));

});