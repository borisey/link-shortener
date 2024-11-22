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

        if (Objects.equals(baseUrl, "localhost")) {
            baseUrl = baseUrl + ":" + request.getServerPort();
        }

        model.addAttribute("links", links);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Мои ссылки");
        model.addAttribute("metaTitle", "Мои ссылки");
        model.addAttribute("metaDescription", "Мои ссылки");
        model.addAttribute("metaKeywords", "Мои ссылки");

        return "link";
    }

    @GetMapping("/link/time-expired")
    public String linkTimeExpired(Model model) {

        // Передаю в вид метатэги
        model.addAttribute("h1", "Время жизни ссылки истекло");
        model.addAttribute("metaTitle", "Время жизни ссылки истекло");
        model.addAttribute("metaDescription", "Время жизни ссылки истекло");
        model.addAttribute("metaKeywords", "Время жизни ссылки истекло");

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
    public String linkLimitReached(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        // Передаю в вид UUID пользователя
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Лимит переходов по ссылке исчерпан");
        model.addAttribute("metaTitle", "Лимит переходов по ссылке исчерпан");
        model.addAttribute("metaDescription", "Лимит переходов по ссылке исчерпан");
        model.addAttribute("metaKeywords", "Лимит переходов по ссылке исчерпан");

        return "limit-reached";
    }

    @GetMapping("/link/not-found")
    public String linkNotFound(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        // Передаю в вид UUID пользователя
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Ссылка была удалена или не найдена");
        model.addAttribute("metaTitle", "Ссылка была удалена или не найдена");
        model.addAttribute("metaDescription", "Ссылка была удалена или не найдена");
        model.addAttribute("metaKeywords", "Ссылка была удалена или не найдена");

        return "not-found";
    }

    @GetMapping("/link/edit-failed")
    public String editFailed(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        // Передаю в вид UUID пользователя
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Редактирование ссылки запрещено");
        model.addAttribute("metaTitle", "Редактирование ссылки запрещено");
        model.addAttribute("metaDescription", "Редактирование ссылки запрещено");
        model.addAttribute("metaKeywords", "Редактирование ссылки запрещено");

        return "edit-failed";
    }

    @GetMapping("/link/delete-failed")
    public String deleteFailed(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        // Передаю в вид UUID пользователя
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Удаление ссылки запрещено");
        model.addAttribute("metaTitle", "Удаление ссылки запрещено");
        model.addAttribute("metaDescription", "Удаление ссылки запрещено");
        model.addAttribute("metaKeywords", "Удаление ссылки запрещено");

        return "delete-failed";
    }

    static String usingUUID() {
        UUID randomUUID = UUID.randomUUID();

        return randomUUID.toString().replaceAll("-", "");
    }

    @GetMapping("/link/{id}/edit")
    public String linkEdit(@CookieValue(value = "UUID", defaultValue = "") String UUID, @PathVariable(value = "id") long id, Model model) {

        // Если ссылку добавил не этот пользователь
        Link userLink = linkRepository.findByIdAndUUID(id, UUID);
        if (userLink == null) {
            return "redirect:/link/edit-failed";
        }

        Optional<Link> link = linkRepository.findById(id);
        ArrayList<Link> res = new ArrayList<>();
        link.ifPresent(res::add);
        model.addAttribute("link", res);

        // Передаю в вид UUID пользователя
        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Редактирование ссылки");
        model.addAttribute("metaTitle", "Редактирование ссылки");
        model.addAttribute("metaDescription", "Редактирование ссылки");
        model.addAttribute("metaKeywords", "Редактирование ссылки");

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

        // Если ссылку добавил не этот пользователь
        Link userLink = linkRepository.findByIdAndUUID(id, UUID);
        if (userLink == null) {
            return "redirect:/link/delete-failed";
        }

        linkRepository.delete(userLink);

        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Ссылка успешно удалена");
        model.addAttribute("metaTitle", "Ссылка успешно удалена");
        model.addAttribute("metaDescription", "Ссылка успешно удалена");
        model.addAttribute("metaKeywords", "Ссылка успешно удалена");

        return "delete-success";
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> linkRedirect(HttpServletRequest request, @PathVariable(value = "shortUrl") String shortUrl, Model model) throws ParseException {
        Link link = linkRepository.findByShortUrl(shortUrl);

        if (link == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/link/not-found")).build();
        }

        LocalDateTime created = link.getCreated()
                .plusDays(1);

        LocalDateTime now = LocalDateTime.now();
        boolean isAfter = now.isAfter(created);

        if (isAfter) {
            linkRepository.delete(link);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/link/time-expired")).build();
        }

        Integer count = link.getCount();

        if (count != null) {
            if (count == 0) {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/link/limit-reached")).build();
            }

            link.setCount(--count);
            linkRepository.save(link);

            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getFullUrl())).build();
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getFullUrl())).build();
    }
}