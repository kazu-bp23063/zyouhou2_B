package com.example.application.ApplicationServer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class DiceController {

    @GetMapping("/api/roll")
    public Map<String, Object> rollDice() {
        Random random = new Random();
        int result = random.nextInt(6) + 1; // 1〜6を生成

        Map<String, Object> response = new HashMap<>();
        response.put("diceResult", result);
        
        return response;
    }
}