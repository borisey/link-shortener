package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.Link;
import com.borisey.link_shortener.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/link")
    public String home(Model model) {
        Iterable<Link> links = linkRepository.findAll();
        model.addAttribute("links", links);
        return "link";
    }

}