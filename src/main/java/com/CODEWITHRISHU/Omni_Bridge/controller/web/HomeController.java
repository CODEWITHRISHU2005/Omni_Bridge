package com.CODEWITHRISHU.Omni_Bridge.controller.web;

import com.CODEWITHRISHU.Omni_Bridge.service.VenueService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final VenueService venueService;

    public HomeController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("venues", venueService.getAll());
        return "index";
    }
}
