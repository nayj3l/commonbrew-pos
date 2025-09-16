package com.commonbrew.pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {

    // Home page (dashboard)
    @GetMapping("/")
    public String showDashboards(Model model) {
            return "dashboard";
    }

}