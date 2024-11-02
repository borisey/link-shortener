package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.Link;
import com.borisey.link_shortener.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/link")
    public String link(Model model) {
        Iterable<Link> links = linkRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("links", links);
        return "link";
    }

    @PostMapping("/link/add")
    public String linkLinkAdd(@RequestParam String fullUrl, Model model) {
        Link link = new Link(fullUrl);
        String randomString = usingUUID();
        String shortUrl = randomString.substring(0, 6);
        link.setShortUrl(shortUrl);
        linkRepository.save(link);
        return "redirect:/link";
    }

    static String usingUUID() {
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString().replaceAll("-", "");
    }

    @GetMapping("/link/{id}")
    public String linkDetails(@PathVariable(value = "id") long id, Model model) {
        if(!linkRepository.existsById(id)) {
            return "redirect:/link";

        }
        Optional<Link> link = linkRepository.findById(id);
        ArrayList<Link> res = new ArrayList<>();
        link.ifPresent(res::add);
        model.addAttribute("link", res);
        return "link-details";
    }

    @GetMapping("/link/{id}/edit")
    public String linkEdit(@PathVariable(value = "id") long id, Model model) {
        if(!linkRepository.existsById(id)) {
            return "redirect:/link";

        }
        Optional<Link> link = linkRepository.findById(id);
        ArrayList<Link> res = new ArrayList<>();
        link.ifPresent(res::add);
        model.addAttribute("link", res);
        return "link-edit";
    }

    @PostMapping("/link/{id}/edit")
    public String linkLinkUpdate(@PathVariable(value = "id") long id, @RequestParam String shortUrl, @RequestParam String fullUrl, Model model) {
        Link link = linkRepository.findById(id).orElseThrow();
        link.setShortUrl(shortUrl);
        link.setFullUrl(fullUrl);
        linkRepository.save(link);

        return "redirect:/link";
    }

    @PostMapping("/link/{id}/remove")
    public String linkLinkRemove(@PathVariable(value = "id") long id, Model model) {
        Link link = linkRepository.findById(id).orElseThrow();
        linkRepository.delete(link);

        return "redirect:/link";
    }

}