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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class LinkController {

    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/link")
    public String link(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletRequest request, Model model) {
        Iterable<Link> links = linkRepository.findByUUID(UUID, Sort.by(Sort.Direction.DESC, "id"));

        // Получаю хост сайта
        String baseUrl = request.getServerName();

        System.out.println(baseUrl);

        model.addAttribute("links", links);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("UUID", UUID);
        return "link";
    }

    @GetMapping("/link/time-expired")
    public String linkTimeExpired(Model model) {

        return "time-expired";
    }

    @PostMapping("/link/add")
    public String linkLinkAdd(HttpServletRequest request, @CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, @RequestParam String fullUrl, @Nullable Integer count, Model model) {
        Link link = new Link(fullUrl);
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Генерация случайной строки
        String randomString = usingUUID();
        String shortUrl = randomString.substring(0, 6);

        link.setShortUrl(shortUrl);
        link.setCount(count);
        link.setCreated(currentDateTime);
        linkRepository.save(link);

        // Генерирую UUID только новым пользователям
        if (Objects.equals(UUID, "")) {
            UUID = randomString.substring(0, 20);
            User user = new User();
            user.setUUID(UUID);
            userRepository.save(user);

            // Получаю хост сайта
            String baseUrl = request.getServerName();

            // Сохраняю UUID в cookie
            Cookie cookie = new Cookie("UUID", UUID);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setHttpOnly(true);
            cookie.setDomain(baseUrl);
            cookie.setPath("/"); // global cookie accessible

            // Добавляю файл cookie в ответ сервера
            response.addCookie(cookie);
        }

        link.setUUID(UUID);
        linkRepository.save(link);

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

    @GetMapping("/link/edit-failed")
    public String editFailed(Model model) {
        return "edit-failed";
    }

    @GetMapping("/link/delete-failed")
    public String deleteFailed(Model model) {
        return "delete-failed";
    }

//    public static String getBaseUrl(HttpServletRequest request) {
//        String baseUrl = ServletUriComponentsBuilder
//                .fromRequestUri(request)
//                .replacePath(null)
//                .build()
//                .toUriString();
//
//        return baseUrl;
//    }

    static String usingUUID() {
        UUID randomUUID = UUID.randomUUID();

        return randomUUID.toString().replaceAll("-", "");
    }

    @GetMapping("/link/{id}")
    public String linkDetails(@PathVariable(value = "id") long id, Model model) {
        if (!linkRepository.existsById(id)) {

            return "redirect:/link";
        }

        Optional<Link> link = linkRepository.findById(id);
        ArrayList<Link> res = new ArrayList<>();
        link.ifPresent(res::add);
        model.addAttribute("link", res);

        return "link-details";
    }

    @GetMapping("/link/{id}/edit")
    public String linkEdit(@CookieValue(value = "UUID", defaultValue = "") String UUID, @PathVariable(value = "id") long id, Model model) {

        Link userLink = linkRepository.findByIdAndUUID(id, UUID);

        // Если статью добавил не этот пользователь
        if (userLink == null) {
            return "redirect:/link/edit-failed";
        }

        Optional<Link> link = linkRepository.findById(id);
        ArrayList<Link> res = new ArrayList<>();
        link.ifPresent(res::add);
        model.addAttribute("link", res);
        model.addAttribute("UUID", UUID);
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
    public String linkLinkRemove(@CookieValue(value = "UUID", defaultValue = "") String UUID, @PathVariable(value = "id") long id, Model model) {

        Link userLink = linkRepository.findByIdAndUUID(id, UUID);

        // Если статью добавил не этот пользователь
        if (userLink == null) {
            return "redirect:/link/delete-failed";
        }

        linkRepository.delete(userLink);

        return "redirect:/link";
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> linkRedirect(HttpServletRequest request, @PathVariable(value = "shortUrl") String shortUrl, Model model) throws ParseException {
        Link link = linkRepository.findByShortUrl(shortUrl);

        // Получаю хост сайта
        String baseUrl = request.getServerName();

        if (link == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(baseUrl + "/link/not-found")).build();
        }

        LocalDateTime created = link.getCreated()
                .plusDays(1);

        LocalDateTime now = LocalDateTime.now();
        boolean isAfter = now.isAfter(created);

        if (isAfter) {
            linkRepository.delete(link);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(baseUrl + "/link/time-expired")).build();
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