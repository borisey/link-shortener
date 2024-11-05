package com.borisey.link_shortener.controllers;

import com.borisey.link_shortener.repo.LinkRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping("/")
    public String linkAdd(@CookieValue(value = "UUID", defaultValue = "") String UUID, Model model) {

        model.addAttribute("UUID", UUID);
        return "link-add";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, Model model) {

        Cookie cookie = new Cookie("UUID", "");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "link-add";
    }
}