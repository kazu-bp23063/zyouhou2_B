package com.example.application.ClientManagementServer.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.application.ClientManagementServer.Entity.*;

import org.springframework.http.ResponseEntity;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

@RestController 
@RequestMapping("/api/auth")
public class AccountManagement {
    private final DatabaseAccess dbAccess;

    public AccountManagement() {
        this.dbAccess = new DatabaseAccess();
        System.out.println("[AccountManagement] インスタンスが作成されました");
    }

    @PostConstruct
    public void init() {
        System.out.println("[AccountManagement] 起動時リセット実行...");
        dbAccess.resetAllLoginStatuses(); 
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginApi(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        User user = login(username, password);
        if (user != null) return ResponseEntity.ok(user);
        System.out.println("[AccountManagement] Login failed for user: " + username);
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerApi(@RequestBody Map<String, String> req) {
        boolean success = registerAccount(req.get("username"), req.get("password"));
        System.out.println("[AccountManagement] Registration " + (success ? "succeeded" : "failed") + " for user: " + req.get("username"));
        return success ? ResponseEntity.ok("Success") : ResponseEntity.badRequest().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutApi(@RequestBody Map<String, String> req) {
        logout(req.get("username"));
        System.out.println("[AccountManagement] User logged out: " + req.get("username"));
        return ResponseEntity.ok("Logged out");
    }
    @GetMapping("/score")
        public ResponseEntity<?> getScore(@RequestParam String username) {
            try {
                RankRecord record = dbAccess.getRankRecordByUsername(username);
                return ResponseEntity.ok(record); 
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        }

    public User login(String username, String password) {
        try {
            User user = dbAccess.getUserByUsername(username);
            if (user != null && user.password().equals(password)) {
                if (dbAccess.getLoginStatusByUsername(username)) return null;
                dbAccess.setLoginStatus(username, true);
                System.out.println("[AccountManagement] User logged in: " + username);
                return user;
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
            System.out.println("[AccountManagement] Exception during login for user: " + username + " - " + e.getMessage());
        }
        return null;
    }

    public boolean registerAccount(String username, String password) {
        try {
            if (dbAccess.getUserByUsername(username) != null) return false;
            String id = UUID.randomUUID().toString();
            dbAccess.createUser(username, password, id);
            System.out.println("[AccountManagement] User registered: " + username);
            return true;
        } catch (Exception e) { return false; }
    }

    public void logout(String username) {
        if (username != null) {
            dbAccess.setLoginStatus(username, false);
            System.out.println("[AccountManagement] User logged out: " + username);
        }
    }
}