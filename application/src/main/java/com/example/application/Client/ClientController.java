package com.example.application.Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Optional;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;



@Controller
public class ClientController {

    @Autowired
    private AccountRepository repository;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/start")
    public String start() {
        return "start";
    }

    @GetMapping("/game")
    public String login() {
        return "game";
    }

    @GetMapping("/score")
    public String register() {
        return "score";
    }

    @GetMapping("/rule")
    public String rule() {
        return "rule";
    }

    @GetMapping("/result")
    public String result() {
        return "result";
    }

    
    @PostMapping("/login-process")
    public String processLogin(@RequestParam("username") String name,
                               @RequestParam("password") String pass,
                               Model model,
                               HttpSession session) {
        
        Optional<Account> user = repository.findById(name);

        if (user.isPresent() && user.get().getPassword().equals(pass)) {
            session.setAttribute("loginName",name);
            return "redirect:/start"; 
        } else {
            model.addAttribute("error","ユーザ名またはパスワードが間違っています!!");
            return "home"; 
        }
    }

    @PostMapping("/register-process")
    public String processSignup(@RequestParam("username") String name,
                                @RequestParam("password") String pass,
                                Model model) {
        
        if(repository.existsById(name)){
            model.addAttribute("error","既に登録されています。");
            return "home";
        }
        
        Account newAccount = new Account();
        newAccount.setUsername(name);
        newAccount.setPassword(pass);
        
        repository.save(newAccount); 

        return "start"; 
    }

}
