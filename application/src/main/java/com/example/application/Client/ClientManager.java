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
public class ClientManager {

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
    public String showScore(HttpSession session, Model model) {
    // 1. セッションからログイン中の名前を取得
    String loginName = (String) session.getAttribute("loginName");

    if (loginName != null) {
        // 2. データベースからユーザー情報を検索
        Optional<Account> userOpt = repository.findById(loginName);
        
        if (userOpt.isPresent()) {
            // 3. データをModelに入れてHTMLに渡す
            model.addAttribute("account", userOpt.get());
        }
    }
    
    return "score"; // score.htmlを表示
}

    @GetMapping("/rule")
    public String rule() {
        return "rule";
    }

    @GetMapping("/result")
    public String result() {
        return "result";
    }

@GetMapping("/logout")
public String logout(HttpSession session) {
    // セッションを無効化してログイン情報を消す
    session.invalidate();
    // ログイン画面（/）へリダイレクト
    return "redirect:/";
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

        return "redirect:/"; 
    }
    //追加しました（高村）

    @GetMapping("/matchingWait")
    public String matchingWait() {
        return "matchingWait";
    }

    


}
