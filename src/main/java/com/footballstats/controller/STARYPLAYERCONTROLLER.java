//package com.footballstats.controller;
//
//import com.footballstats.model.Player;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//@Controller
//public class STARYPLAYERCONTROLLER {
//
//    @GetMapping("/players")
//    public String players(Model model) {
//
//        List<Player> players = List.of(
//                new Player(
//                        1L,
//                        "Robert",
//                        "Lewandowski",
//                        LocalDate.of(1988, 8, 21),
//                        37,
//                        185,
//                        "Poland",
//                        "pl",
//                        "Striker",
//                        "Right",
//                        9,
//                        "FC Barcelona",
//                        LocalDate.of(2022, 7, 19),
//                        LocalDate.of(2026, 6, 30),
//                        LocalDate.of(2024, 1, 1),
//                        new BigDecimal("15000000"),
//                        true,
//                        "https://via.placeholder.com/80"
//                )
//        );
//
//        model.addAttribute("players", players);
//
//        return "players";
//    }
//}