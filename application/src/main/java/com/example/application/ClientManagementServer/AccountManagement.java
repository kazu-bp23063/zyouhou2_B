package com.example.application.ClientManagementServer;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

@RestController // ✅ REST API として動作
@RequestMapping("/api/auth")
public class AccountManagement {
    private final DatabaseAccess dbAccess;

    public AccountManagement() {
        this.dbAccess = new DatabaseAccess();
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
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerApi(@RequestBody Map<String, String> req) {
        boolean success = registerAccount(req.get("username"), req.get("password"));
        return success ? ResponseEntity.ok("Success") : ResponseEntity.badRequest().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutApi(@RequestBody Map<String, String> req) {
        logout(req.get("username"));
        return ResponseEntity.ok("Logged out");
    }

    public User login(String username, String password) {
        try {
            User user = dbAccess.getUserByUsername(username);
            if (user != null && user.password().equals(password)) {
                if (dbAccess.getLoginStatusByUsername(username)) return null;
                dbAccess.setLoginStatus(username, true);
                return user;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean registerAccount(String username, String password) {
        try {
            if (dbAccess.getUserByUsername(username) != null) return false;
            String id = UUID.randomUUID().toString();
            dbAccess.createUser(username, password, id);
            return true;
        } catch (Exception e) { return false; }
    }

    public void logout(String username) {
        if (username != null) dbAccess.setLoginStatus(username, false);
    }
}