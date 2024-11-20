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

import java.net.MalformedURLException;
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
        return "link-add";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) throws MalformedURLException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName() + " " + cookie.getValue());
                System.out.println(cookie.getDomain());

                cookie.setMaxAge(0);
                // Получаю хост сайта
//                String baseUrl = request.getServerName();
//                cookie.setDomain(baseUrl);
                cookie.setPath("/"); // global cookie accessible
                response.addCookie(cookie);
            }

        return "logout-success";
    }

    @GetMapping("/login")
    public String login(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        if (!Objects.equals(UUID, "")) {
            return "redirect:/";
        }

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
    public String authFailed(HttpServletResponse response, Model model) {
        return "auth-failed";
    }

    @GetMapping("/auth-success")
    public String authSuccess(@CookieValue(value = "UUID", defaultValue = "") String UUID, HttpServletResponse response, Model model) {

        model.addAttribute("UUID", UUID);
        return "auth-success";
    }
}