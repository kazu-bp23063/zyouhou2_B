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


    @GetMapping("/") 
    public String home() {
        System.out.println("Accessed home page");
        return "home"; 
    }

    @GetMapping("/start") 
    public String start() { 
        System.out.println("Accessed start page");
        return "start"; 
    }

    @PostMapping("/login-process")
    public String processLogin(@RequestParam String username, @RequestParam String password,
                               HttpSession session, Model model) {
        try {
            Map<String, String> request = Map.of("username", username, "password", password);
            Map response = restTemplate.postForObject(AUTH_API_URL + "/login", request, Map.class);
            if (response != null) {
                session.setAttribute("loginName", username);
                System.out.println("User " + username + " logged in successfully.");
                return "redirect:/start";
            }
        } catch (Exception e) {
            model.addAttribute("error", "ログイン失敗、または既にログイン中です。");
            System.out.println("Login failed for user " + username + ": " + e.getMessage());
        }
        return "home";
    }

    @PostMapping("/register-process")
    public String processSignup(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            restTemplate.postForObject(AUTH_API_URL + "/register", 
                Map.of("username", username, "password", password), String.class);
                System.out.println("User " + username + " registered successfully.");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "登録に失敗しました。");
            System.out.println("Registration failed for user " + username + ": " + e.getMessage());
            return "home";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String user = (String) session.getAttribute("loginName");
        if (user != null) {
            try {
                restTemplate.postForObject(AUTH_API_URL + "/logout", Map.of("username", user), String.class);
                System.out.println("User " + user + " logged out successfully.");
            } catch (Exception e) { 
                e.printStackTrace(); 
                System.out.println("Logout failed for user " + user + ": " + e.getMessage());
            }
        }
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/matchingWait")
    public String matchingWait() {
        System.out.println("Accessed matchingWait page");
        return "matchingwait";
    }

    @GetMapping("/rule")
    public String rule() {
        System.out.println("Accessed rule page");
        return "rule";
    }

 @GetMapping("/game")
    public String index(Model model, HttpSession session) {
        model.addAttribute("earnedUnits", 0);
        model.addAttribute("expectedUnits", 25);
        model.addAttribute("result", "ダイスを振ってください");
        System.out.println("Accessed game page");
        return "game";
    }

    @GetMapping("/score")
    public String showScorePage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("loginName");
        if (username == null) return "redirect:/";

        try {
            String url = AUTH_API_URL + "/score?username=" + username;
            
            // APIから JSON を RankRecord オブジェクトとして受け取る
            Map<String, Object> record = restTemplate.getForObject(url, Map.class);

            model.addAttribute("username", username);
            model.addAttribute("record", record); // Thymeleafへ渡す
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "戦績の取得に失敗しました。");
        }
        
        return "score"; // score.html を表示
    }
    
    
}