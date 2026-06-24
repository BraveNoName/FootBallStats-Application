package com.footballstats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutSystemController {

    @GetMapping("/about-system")
    public String aboutSystemPage() {
        return "about_system";
    }
}
