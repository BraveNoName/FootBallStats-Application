package com.footballstats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeagueController {

    @GetMapping("/leagues")
    public String leagues() {
        return "leagues";
    }
}