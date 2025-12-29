package com.example.application.Client.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import java.util.Map;


@Controller
public class ClientManager {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String AUTH_API_URL = "http://localhost:8080/api/auth";


    @GetMapping("/") public String home() { return "home"; }
    @GetMapping("/start") public String start() { return "start"; }

    @PostMapping("/login-process")
    public String processLogin(@RequestParam String username, @RequestParam String password,
                               HttpSession session, Model model) {
        try {
            Map<String, String> request = Map.of("username", username, "password", password);
            Map response = restTemplate.postForObject(AUTH_API_URL + "/login", request, Map.class);
            if (response != null) {
                session.setAttribute("loginName", username);
                return "redirect:/start";
            }
        } catch (Exception e) {
            model.addAttribute("error", "ログイン失敗、または既にログイン中です。");
        }
        return "home";
    }

    @PostMapping("/register-process")
    public String processSignup(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            restTemplate.postForObject(AUTH_API_URL + "/register", 
                Map.of("username", username, "password", password), String.class);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "登録に失敗しました。");
            return "home";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String user = (String) session.getAttribute("loginName");
        if (user != null) {
            try {
                restTemplate.postForObject(AUTH_API_URL + "/logout", Map.of("username", user), String.class);
            } catch (Exception e) { e.printStackTrace(); }
        }
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/matchingWait")
    public String matchingWait() {
        return "matchingwait";
    }

    @GetMapping("/rule")
    public String rule() {
        return "rule";
    }

 @GetMapping("/game")
    public String index(Model model, HttpSession session) {
        model.addAttribute("earnedUnits", 0);
        model.addAttribute("expectedUnits", 25);
        model.addAttribute("result", "ダイスを振ってください");
        return "game";
    }
    
}