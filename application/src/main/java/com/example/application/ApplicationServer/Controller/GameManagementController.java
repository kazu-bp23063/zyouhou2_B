package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ClientManagementServer.CommunicationController;
import com.example.application.ApplicationServer.Entity.GameEvent;
import com.google.gson.Gson;
import jakarta.websocket.Session;
import java.util.HashMap;
import java.util.Map;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final RoomManager roomManager = new RoomManager();
    private final DiceController diceController = new DiceController();

    public void processGameMessage(String json, Session session) {
        Map<String, Object> msg = gson.fromJson(json, Map.class);
        String taskName = (String) msg.get("taskName");
        String playerId = (String) msg.get("playerId");

        if (playerId != null) {
            CommunicationController.userSessions.put(playerId, session);
        }

        switch (taskName) {
            case "GAME_JOIN" -> System.out.println("[Game] Player " + playerId + " joined.");
            case "GAME_ROLL" -> handleRoll(msg);
        }
    }

    private void handleRoll(Map<String, Object> msg) {
        String roomId = (String) msg.get("roomId");
        String playerId = (String) msg.get("playerId");
        String itemType = (String) msg.get("itemType"); // アイテムの種類を取得
        
        // 数値はGsonによりDoubleとしてデコードされるため変換が必要
        Integer targetValue = null;
        if (msg.get("targetValue") != null) {
            targetValue = ((Double) msg.get("targetValue")).intValue();
        }

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        int currentTurnIndex = room.getTurnIndex();
        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

        if (!currentPlayer.getId().equals(playerId)) return;

        // --- 1. ダイス実行（アイテム効果を反映） ---
        int rolledNumber;
        if ("DOUBLE".equals(itemType)) {
            rolledNumber = diceController.roll() + diceController.roll();
            System.out.println("[Item] DOUBLE used. Roll: " + rolledNumber);
        } else if ("JUST".equals(itemType) && targetValue != null) {
            rolledNumber = targetValue;
            System.out.println("[Item] JUST used. Roll: " + rolledNumber);
        } else {
            rolledNumber = diceController.roll();
        }

        // --- 2. 移動と一周（単位獲得）判定 ---
        int oldPos = currentPlayer.getCurrentPosition();
        int newPos = (oldPos + rolledNumber) % 20;

        if (oldPos + rolledNumber >= 20) {
            currentPlayer.setEarnedUnits(currentPlayer.getEarnedUnits() + currentPlayer.getExpectedUnits());
            System.out.println("[Game] " + playerId + " passed START. Units added.");
        }
        
        // 一旦位置をセット
        currentPlayer.setCurrentPosition(newPos);

        // --- 3. 【追加】マスのイベント判定 ---
        String eventMsg = GameEvent.execute(currentPlayer); // GameEvent呼び出し
        
        // イベントによって位置が変わっている可能性があるため最新値を取得
        int finalPos = currentPlayer.getCurrentPosition();

        // --- 4. ターン更新と卒業判定 ---
        int nextTurnIndex = (currentTurnIndex + 1) % room.getPlayers().size();
        room.setTurnIndex(nextTurnIndex);
        Player nextPlayer = room.getPlayers().get(nextTurnIndex);

        boolean isGraduated = currentPlayer.getEarnedUnits() >= 124; // 卒業判定しきい値を124へ

        // --- 5. レスポンス送信 ---
        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "GAME_UPDATE");
        response.put("lastPlayerId", playerId);
        response.put("diceValue", rolledNumber);
        response.put("newPosition", finalPos); // イベント適用後の位置
        response.put("earnedUnits", currentPlayer.getEarnedUnits());
        response.put("expectedUnits", currentPlayer.getExpectedUnits()); // 画面更新用に追加
        response.put("nextPlayerId", nextPlayer.getId());
        response.put("isGraduated", isGraduated);
        response.put("message", eventMsg); // イベントメッセージを通知

        broadcastToRoom(room, response);
    }

    private void broadcastToRoom(Room room, Object messageObj) {
        String json = gson.toJson(messageObj);
        for (Player p : room.getPlayers()) {
            CommunicationController.sendToUser(p.getId(), json);
        }
    }
}