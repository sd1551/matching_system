package com.diplom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {

    @GetMapping("/")
    public String homePage() {
        return "redirect:/auth/login";
    }

    @GetMapping("/hub")
    public String hubPage() {
        return "hub";
    }
}