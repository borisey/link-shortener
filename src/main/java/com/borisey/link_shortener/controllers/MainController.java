package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.User;
import com.borisey.link_shortener.repo.LinkRepository;
import com.borisey.link_shortener.repo.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
public class MainController {

    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Сервис коротких ссылок");
        model.addAttribute("metaTitle", "Сервис коротких ссылок");
        model.addAttribute("metaDescription", "Сервис коротких ссылок");
        model.addAttribute("metaKeywords", "Сервис коротких ссылок");

        return "link-add";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                // Получаю хост сайта
                String baseUrl = request.getServerName();
                cookie.setDomain(baseUrl);
                response.addCookie(cookie);
            }
        }

        // Передаю в вид метатэги
        model.addAttribute("h1", "Вы успешно вышли");
        model.addAttribute("metaTitle", "Вы успешно вышли");
        model.addAttribute("metaDescription", "Вы успешно вышли");
        model.addAttribute("metaKeywords", "Вы успешно вышли");

        return "logout-success";
    }

    @GetMapping("/login")
    public String login(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        if (!Objects.equals(UUID, "")) {
            return "redirect:/";
        }

        // Передаю в вид метатэги
        model.addAttribute("h1", "Авторизация");
        model.addAttribute("metaTitle", "Авторизация");
        model.addAttribute("metaDescription", "Авторизация");
        model.addAttribute("metaKeywords", "Авторизация");

        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, @RequestParam String UUID, Model model) {
        User userIdentity = userRepository.findByUUID(UUID);

        // Получаю хост сайта
        String baseUrl = request.getServerName();

        if (userIdentity == null) {
            return "redirect:/auth-failed";
        } else {
            Cookie cookie = new Cookie("UUID", UUID);
            cookie.setDomain(baseUrl);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/auth-success";
        }
    }

    @GetMapping("/auth-failed")
    public String authFailed(Model model) {

        // Передаю в вид метатэги
        model.addAttribute("h1", "Пользователь с таким UUID не найден");
        model.addAttribute("metaTitle", "Пользователь с таким UUID не найден");
        model.addAttribute("metaDescription", "Пользователь с таким UUID не найден");
        model.addAttribute("metaKeywords", "Пользователь с таким UUID не найден");

        return "auth-failed";
    }

    @GetMapping("/auth-success")
    public String authSuccess(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        model.addAttribute("UUID", UUID);

        // Передаю в вид метатэги
        model.addAttribute("h1", "Вы успешно вошли на сайт");
        model.addAttribute("metaTitle", "Вы успешно вошли на сайт");
        model.addAttribute("metaDescription", "Вы успешно вошли на сайт");
        model.addAttribute("metaKeywords", "Вы успешно вошли на сайт");

        return "auth-success";
    }

    @GetMapping("/robots.txt")
    public String getRobots() {
        return "robots.txt";
    }

    @GetMapping("/sitemap.xml")
    public String getSitemap() {
        return "sitemap.xml";
    }
}