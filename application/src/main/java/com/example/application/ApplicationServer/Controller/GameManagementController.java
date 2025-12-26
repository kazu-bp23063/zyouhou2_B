package com.example.application.ApplicationServer.Controller;

import java.util.HashMap;
import java.util.Map;

import com.example.application.ApplicationServer.Entity.GameEvent;
import com.example.application.ApplicationServer.Entity.GameMap;
import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ClientManagementServer.CommunicationController;
import com.google.gson.Gson;

import jakarta.websocket.Session;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final RoomManager roomManager = new RoomManager();
    private final DiceController diceController = new DiceController();
    private final GameMap gameMap = new GameMap(); 

    public void processGameMessage(String json, Session session) {
        Map<String, Object> msg = gson.fromJson(json, Map.class);
        String taskName = (String) msg.get("taskName");
        String playerId = (String) msg.get("playerId");
        String roomId = (String) msg.get("roomId"); 

        if (playerId != null) {
            CommunicationController.userSessions.put(playerId, session);
            resetAFKCount(roomId, playerId);
            System.out.println("[Game] Session registered for: " + playerId);
        }

        switch (taskName) {
            case "GAME_JOIN" -> {
                System.out.println("[Game] Player " + playerId + " joined.");
            }
            case "GAME_ROLL" -> handleRoll(msg);
        }
    }

    private void handleRoll(Map<String, Object> msg) {
        String roomId = (String) msg.get("roomId");
        String playerId = (String) msg.get("playerId");
        String itemType = (String) msg.get("itemType");
        Double targetValDouble = (Double) msg.get("targetValue");
        Integer targetValue = targetValDouble != null ? targetValDouble.intValue() : null;

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        int currentTurnIndex = room.getTurnIndex();
        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

        if (!currentPlayer.getId().equals(playerId)) return;

        // --- 1. ダイス実行（アイテム効果を反映） ---
        int rolledNumber= diceController.executeRoll(itemType, targetValue);
        if ("DOUBLE".equals(itemType)) {
            rolledNumber = diceController.roll() + diceController.roll();
            System.out.println("[Item] DOUBLE used. Roll: " + rolledNumber);
        } else if ("JUST".equals(itemType) && targetValue != null) {
            rolledNumber = targetValue;
            System.out.println("[Item] JUST used. Roll: " + rolledNumber);
        } else {
            rolledNumber = diceController.roll();
        }

        // 移動計算
        int oldPos = currentPlayer.getCurrentPosition();
        int tempPos = oldPos + rolledNumber;
        
        // 周回判定
        if (tempPos >= 20) {
            callCreditManager(currentPlayer, true); 
            System.out.println("[Game] スタート通過（周回ボーナス付与）");
        }
        
        int newPos = tempPos % 20;
        currentPlayer.setCurrentPosition(newPos);

        // イベントチェック
        GameEvent event = gameMap.getGameEvent(newPos);
        if (event != null) {
            System.out.println("[Game] Event: " + event.getEventContent());
            callCreditManager(currentPlayer, false, event.getCreditAdjustmentValue());
            
            if (event.getEventEffect() == GameEvent.EFFECT_SKIP) {
                currentPlayer.setSkipped(true);
            }
            else if (event.getEventEffect() == GameEvent.EFFECT_BACK) {
                int backPos = (newPos - 1 + 20) % 20;
                currentPlayer.setCurrentPosition(backPos);
            }
        }

        // ターン送り

        int nextTurnIndex = (currentTurnIndex + 1) % room.getPlayers().size();
        room.setTurnIndex(nextTurnIndex);
        Player nextPlayer = room.getPlayers().get(nextTurnIndex);


        // 次のプレイヤーのスキップ処理
        if (nextPlayer.isSkipped()) {
            System.out.println("[Game] " + nextPlayer.getName() + " はスキップされます。");
            nextPlayer.setSkipped(false);
            nextTurnIndex = (nextTurnIndex + 1) % room.getPlayers().size();
            room.setTurnIndex(nextTurnIndex);
            nextPlayer = room.getPlayers().get(nextTurnIndex);
        }

        boolean isGraduated = currentPlayer.checkGraduationRequirement();

        checkStatus(room);

        // クライアント通知

        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "GAME_UPDATE");
        response.put("lastPlayerId", playerId);
        response.put("diceValue", rolledNumber);
        response.put("newPosition", currentPlayer.getCurrentPosition());
        response.put("earnedUnits", currentPlayer.getEarnedUnits()); 
        
        response.put("nextPlayerId", nextPlayer.getId());
        response.put("isGraduated", isGraduated);
        
        if (event != null) {
            response.put("eventMessage", event.getEventContent());
        }

        broadcastToRoom(room, response);
        
        if (isGraduated) {
            endGame(room);
        }
    }

    // 周回時の処理
    private void callCreditManager(Player player, boolean isLap) {
        if (isLap) {
            int earned = player.getEarnedUnits();
            int expected = player.getExpectedUnits();
            player.setEarnedUnits(earned + expected);
            player.setExpectedUnits(25);
        }
    }

    // イベントによる増減
    private void callCreditManager(Player player, boolean isLap, int adjustment) {
        int current = player.getExpectedUnits();
        player.setExpectedUnits(current + adjustment);
    }

    private void checkStatus(Room room) {
        System.out.println("--- Status ---");
        for(Player p : room.getPlayers()){
            System.out.printf("%s: Pos=%d, Earned=%d, Next=%d\n", 
                p.getName(), p.getCurrentPosition(), p.getEarnedUnits(), p.getExpectedUnits());
        }
    }

    private void endGame(Room room) {
        System.out.println("========== GAME FINISHED ==========");
        Map<String, Object> endMsg = new HashMap<>();
        endMsg.put("taskName", "GAME_END");
        broadcastToRoom(room, endMsg);
    }

    private void resetAFKCount(String roomId, String playerId) {
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room != null) {
            room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .ifPresent(p -> p.setAfkCount(0));
        }
    }

    private void broadcastToRoom(Room room, Object messageObj) {
        String json = gson.toJson(messageObj);
        for (Player p : room.getPlayers()) {
            CommunicationController.sendToUser(p.getId(), json);
        }
    }
}