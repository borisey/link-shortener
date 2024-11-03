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
        Link link = linkRepository.findByShortUrl(shortUrl);

        Integer count = link.getCount();
        if (count == 0) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:8080/link")).build();
        }

        if (count != null) {
            link.setCount(--count);
            linkRepository.save(link);
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getFullUrl())).build();
    }
}