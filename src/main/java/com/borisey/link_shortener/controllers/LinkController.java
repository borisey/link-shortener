package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.Link;
import com.borisey.link_shortener.repo.LinkRepository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/link")
    public String link(HttpServletRequest request, Model model) {
        Iterable<Link> links = linkRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        String baseUrl = getBaseUrl(request);
        model.addAttribute("links", links);
        model.addAttribute("baseUrl", baseUrl);
        return "link";
    }

    @PostMapping("/link/add")
    public String linkLinkAdd(@RequestParam String fullUrl, @Nullable Integer count, Model model) {
        Link link = new Link(fullUrl);
        String randomString = usingUUID();
        String shortUrl = randomString.substring(0, 6);
        link.setShortUrl(shortUrl);
        link.setCount(count);
        linkRepository.save(link);
        return "redirect:/link";
    }

    public static String getBaseUrl(HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        return baseUrl;
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