package com.footballstats.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("!mongodb")
public class SearchController {

    @GetMapping("/api/search")
    public List<SearchResult> search(@RequestParam String q) {

        String query = q.toLowerCase();

        return getResults()
                .stream()
                .filter(item ->
                        item.name().toLowerCase().contains(query)
                                || item.type().toLowerCase().contains(query)
                )
                .limit(8)
                .toList();
    }

    private List<SearchResult> getResults() {

        return List.of(

                new SearchResult(
                        "Premier League",
                        "League",
                        "/leagues/england",
                        "/Images/leagues/England - Premier League/premierleague.png"
                ),

                new SearchResult(
                        "LaLiga",
                        "League",
                        "/leagues/spain",
                        "/Images/leagues/Spain - LaLiga/laliga.png"
                ),

                new SearchResult(
                        "Serie A",
                        "League",
                        "/leagues/italy",
                        "/Images/leagues/Italy - Serie A/seriea.png"
                ),

                new SearchResult(
                        "Bundesliga",
                        "League",
                        "/leagues/germany",
                        "/Images/leagues/Germany - Bundesliga/bundesliga.png"
                ),

                new SearchResult(
                        "Ligue 1",
                        "League",
                        "/leagues/france",
                        "/Images/leagues/France - Ligue 1/ligue1.png"
                ),

                new SearchResult(
                        "Ekstraklasa",
                        "League",
                        "/leagues/poland",
                        "/Images/leagues/Poland - PKO BP Ekstraklasa/pkobpekstraklasa.png"
                ),

                new SearchResult(
                        "Liga Portugal",
                        "League",
                        "/leagues/portugal",
                        "/Images/leagues/Portugal - Liga Portugal/ligaportugal.png"
                ),

                new SearchResult(
                        "Eredivisie",
                        "League",
                        "/leagues/netherlands",
                        "/Images/leagues/Netherlands - Eredivisie/eredivisie.png"
                ),

                new SearchResult(
                        "Süper Lig",
                        "League",
                        "/leagues/turkiye",
                        "/Images/leagues/Turkiye - Super Lig/superlig.png"
                ),

                new SearchResult(
                        "Scottish Premiership",
                        "League",
                        "/leagues/scotland",
                        "/Images/leagues/Scotland - Scottish Premiership/scottishpremiership.png"
                ),

                new SearchResult(
                        "Belgian Pro League",
                        "League",
                        "/leagues/belgium",
                        "/Images/leagues/Belgium - Jupiler Pro League/proleague.png"
                ),

                new SearchResult(
                        "Swiss Super League",
                        "League",
                        "/leagues/switzerland",
                        "/Images/leagues/Switzerland - Super League/superleague.png"
                ),

                new SearchResult(
                        "Austrian Bundesliga",
                        "League",
                        "/leagues/austria",
                        "/Images/leagues/Austria - Bundesliga/austriabundesliga.png"
                ),

                new SearchResult(
                        "Danish Superliga",
                        "League",
                        "/leagues/denmark",
                        "/Images/leagues/Denmark - Superliga/superliga.png"
                ),

                new SearchResult(
                        "Croatian HNL",
                        "League",
                        "/leagues/croatia",
                        "/Images/leagues/Croatia - SuperSport HNL/supersporthnl.png"
                ),

                new SearchResult(
                        "Czech Chance Liga",
                        "League",
                        "/leagues/czech-republic",
                        "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png"
                ),

                new SearchResult(
                        "Allsvenskan",
                        "League",
                        "/leagues/sweden",
                        "/Images/leagues/Sweden - Allsvenskan/allsvenskan.png"
                ),

                new SearchResult(
                        "Eliteserien",
                        "League",
                        "/leagues/norway",
                        "/Images/leagues/Norway - Eliteserien/eliteserien.png"
                ),

                new SearchResult(
                        "Romania SuperLiga",
                        "League",
                        "/leagues/romania",
                        "/Images/leagues/Romania - SuperLiga/romaniasuperliga.png"
                ),

                new SearchResult(
                        "Super liga Srbije",
                        "League",
                        "/leagues/serbia",
                        "/Images/leagues/Serbia - Super liga Srbije/superligaserbije.png"
                ),

                new SearchResult(
                        "Premier Liga",
                        "League",
                        "/leagues/ukraine",
                        "/Images/leagues/Ukraine - Premier Liga/premierliga.png"
                ),

                new SearchResult(
                        "efbet Liga",
                        "League",
                        "/leagues/bulgaria",
                        "/Images/leagues/Bulgaria - efbet Liga/efbetliga.png"
                ),

                new SearchResult(
                        "Super League 1",
                        "League",
                        "/leagues/greece",
                        "/Images/leagues/Greece - Super League 1/superleague1.png"
                ),

                new SearchResult(
                        "Ligat ha Al",
                        "League",
                        "/leagues/israel",
                        "/Images/leagues/Israel - Ligat ha Al/ligathaai.png"
                ),

                new SearchResult(
                        "Russian Premier Liga",
                        "League",
                        "/leagues/russia",
                        "/Images/leagues/Russia - Premier Liga/russiapremierliga.jpg"
                )
                ,

                new SearchResult(
                        "UEFA Champions League",
                        "European Competition",
                        "/matches/europe/champions-league",
                        "/Images/european_leagues/UCL.png"
                ),

                new SearchResult(
                        "UEFA Europa League",
                        "European Competition",
                        "/matches/europe/europa-league",
                        "/Images/european_leagues/UEL.png"
                ),

                new SearchResult(
                        "UEFA Conference League",
                        "European Competition",
                        "/matches/europe/conference-league",
                        "/Images/european_leagues/UECL.png"
                ),
                new SearchResult(
                        "Hungary - Nemezeti Bajnoksag",
                        "League",
                        "/leagues/hungary",
                        "/Images/leagues/Hungary - Nemezeti Bajnoksag/NemzetiBanjnoksag.png"
                ),
                new SearchResult(
                        "Moldova - Superliga",
                        "League",
                        "/leagues/moldova",
                        "/Images/leagues/Moldova - Superliga/superliga.png"
                ),
                new SearchResult(
                        "Republic of Ireland - Ireland Premier Division",
                        "League",
                        "/leagues/republic-of-ireland",
                        "/Images/leagues/Republic of Ireland - Ireland Premier Division/irelandpremierdivision.png"
                )
        );
    }

    public record SearchResult(
            String name,
            String type,
            String url,
            String logo
    ) {}
}