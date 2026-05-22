document.addEventListener("DOMContentLoaded", () => {
    const hiddenTitles = {
        pl: "👈 Kliknij aby powrócić!",
        en: "👈 Click to come back!",
        de: "👈 Klicke um zurückzukehren!",
        es: "👈 ¡Haz clic para volver!",
        fr: "👈 Cliquez pour revenir !",
        it: "👈 Clicca per tornare!",
        pt: "👈 Clique para voltar!",
        tr: "👈 Geri dönmek için tıkla!"
    };

    const originalTitle = document.title;
    const lang = (document.documentElement.lang || "en").substring(0, 2);
    const hiddenTitle = hiddenTitles[lang] || hiddenTitles.en;
    let interval;

    document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
            interval = setInterval(() => {
                document.title = document.title === originalTitle ? hiddenTitle : originalTitle;
            }, 1000);
        } else {
            clearInterval(interval);
            document.title = originalTitle;
        }
    });
});