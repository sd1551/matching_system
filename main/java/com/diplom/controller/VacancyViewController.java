package com.diplom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vacancies")
public class VacancyViewController {

    @GetMapping("/create")
    public String createPage() {
        return "create";
    }

    @GetMapping
    public String vacanciesPage() {
        return "vacancies";
    }
}