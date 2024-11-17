package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.models.User;
import com.borisey.link_shortener.repo.LinkRepository;
import com.borisey.link_shortener.repo.UserRepository;
import jakarta.servlet.http.Cookie;
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
    public String linkAdd(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        model.addAttribute("UUID", UUID);
        return "link-add";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, Model model) {

        Cookie cookie = new Cookie("UUID", null);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "link-add";
    }

    @GetMapping("/login")
    public String login(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        if (!Objects.equals(UUID, "")) {
            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletResponse response, @RequestParam String UUID, Model model) {
        User userIdentity = userRepository.findByUUID(UUID);

        if (userIdentity == null) {
            return "redirect:/auth-failed";
        } else {
            Cookie cookie = new Cookie("UUID", UUID);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/auth-success";
        }
    }

    @GetMapping("/auth-failed")
    public String authFailed(HttpServletResponse response, Model model) {
        return "auth-failed";
    }

    @GetMapping("/auth-success")
    public String authSuccess(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        model.addAttribute("UUID", UUID);
        return "auth-success";
    }
}