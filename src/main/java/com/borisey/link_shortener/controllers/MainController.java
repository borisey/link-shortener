package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/")
    public String linkAdd(@CookieValue(value = "UUID", defaultValue = "defaultValue") String UUID, Model model) {

        model.addAttribute("UUID", UUID);
        return "link-add";
    }
}