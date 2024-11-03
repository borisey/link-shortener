package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.Link;
import com.borisey.link_shortener.models.User;
import com.borisey.link_shortener.repo.LinkRepository;
import com.borisey.link_shortener.repo.UserRepository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/link")
    public String link(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletRequest request, Model model) {
        Iterable<Link> links = linkRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        String baseUrl = getBaseUrl(request);
        model.addAttribute("links", links);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("UUID", UUID);
        return "link";
    }

    @PostMapping("/link/add")
    public String linkLinkAdd(HttpServletResponse response, @RequestParam String fullUrl, String UUID, @Nullable Integer count, Model model) {
        Link link = new Link(fullUrl);
        String randomString = usingUUID();
        String shortUrl = randomString.substring(0, 6);
        link.setShortUrl(shortUrl);
        link.setCount(count);
        linkRepository.save(link);

        // Генерирую UUID только новым пользователям
        if (Objects.equals(UUID, "")) {
            UUID = randomString.substring(0, 20);
            User user = new User(UUID);
            userRepository.save(user);

            // Сохраняю UUID в cookie
            Cookie cookie = new Cookie("UUID", UUID);
            cookie.setPath("/"); // global cookie accessible
            // Добавляю файл cookie в ответ сервера
            response.addCookie(cookie);
        }

        return "redirect:/link";
    }

    @GetMapping("/link/limit-reached")
    public String linkLimitReached(Model model) {
        return "limit-reached";
    }

    @GetMapping("/link/not-found")
    public String linkNotFound(Model model) {
        return "not-found";
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
    public String linkLinkUpdate(@PathVariable(value = "id") long id, @RequestParam String fullUrl, Integer count, Model model) {
        Link link = linkRepository.findById(id).orElseThrow();
        link.setFullUrl(fullUrl);
        link.setCount(count);
        linkRepository.save(link);

        return "redirect:/link";
    }

    @GetMapping("/link/{id}/delete")
    public String linkLinkRemove(@PathVariable(value = "id") long id, Model model) {
        Link link = linkRepository.findById(id).orElseThrow();
        linkRepository.delete(link);

        return "redirect:/link";
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> linkRedirect(HttpServletRequest request, @PathVariable(value = "shortUrl") String shortUrl, Model model) {
        Link link = linkRepository.findByShortUrl(shortUrl);

        String baseUrl = getBaseUrl(request);

        if (link == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(baseUrl + "/link/not-found")).build();
        }

        Integer count = link.getCount();

        if (count != null) {
            if (count == 0) {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(baseUrl + "/link/limit-reached")).build();
            }

            link.setCount(--count);
            linkRepository.save(link);

            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getFullUrl())).build();
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getFullUrl())).build();
    }

}