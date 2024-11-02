package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.Link;
import com.borisey.link_shortener.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/")
    public String linkAdd(Model model) {
        return "link-add";
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> linkRedirect(@PathVariable(value = "shortUrl") String shortUrl, Model model) {

        System.out.println(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("https://ya.ru/")).build();
    }

}