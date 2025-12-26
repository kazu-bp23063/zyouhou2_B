package com.example.application.ApplicationServer.Controller;

import com.example.application.Client.Entity.ClientToClientManagementMessage;
import com.google.gson.Gson;
import jakarta.websocket.Session;
import java.util.HashMap;
import java.util.Map;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final DiceController diceController = new DiceController();
    private final RoomManager roomManager = new RoomManager();

    public void processGameMessage(String json, Session session) {
        ClientToClientManagementMessage msg = gson.fromJson(json, ClientToClientManagementMessage.class);
        System.out.println("[GameManagement] Task: " + msg.getTaskName());

        switch (msg.getTaskName()) {
            case "ROLL_DICE" -> handleRollDice(msg, session);
            // 他にも "USE_ITEM" などの処理をここに増やせます
        }
    }

    private void handleRollDice(ClientToClientManagementMessage msg, Session session) {
        // ダイスを振る
        int rolledNumber = diceController.roll();
        
        // 結果を返信用のMapに格納
        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "DICE_RESULT");
        response.put("rolledNumber", rolledNumber);
        response.put("userId", msg.getUserId());

        // クライアントへ送信
        session.getAsyncRemote().sendText(gson.toJson(response));
    }
}