package com.example.application.ApplicationServer.Controller;

import java.util.HashMap;
import java.util.Map;
import com.example.application.ApplicationServer.Entity.*; // 必要に応じて
import com.google.gson.Gson;
import jakarta.websocket.Session;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final DiceController diceController = new DiceController();
    private final GameMap gameMap = new GameMap(); 

    private RoomManager getRoomManager() {
        return RoomManager.instance;
    }



public void processGameMessage(String json, Session session) {
    Map<String, Object> msg = gson.fromJson(json, Map.class);
    String taskName = (String) msg.get("taskName");
    String playerId = (String) msg.get("playerId");
    String roomId = (String) msg.get("roomId"); 

    if (playerId != null) {
        SessionManager.userSessions.put(playerId, session); 
        resetAFKCount(roomId, playerId);
        System.out.println("[Game] Session registered for: " + playerId);
    }

    switch (taskName) {
        case "GAME_JOIN" -> {
            RoomManager rm = getRoomManager();
            Room room = rm.getRoom(roomId);
            if (room != null) {
                boolean exists = room.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
                if (!exists) {
                    Player newPlayer = new Player(playerId, "red");
                    room.getPlayers().add(newPlayer);
                    System.out.println("[Game] 部屋 " + roomId + " にプレイヤー " + playerId + " を追加しました。現在: " + room.getPlayers().size() + "人");
                }
            }
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

     RoomManager rm = getRoomManager();
    if (rm == null) {
        System.out.println("[Game] RoomManager が null です。");
        return;
    }
    
    Room room = rm.getRoom(roomId);
    if (room == null) {
        System.out.println("[Game] 部屋が見つかりません: " + roomId);
        return;
    }

    System.out.println("[Game] 部屋を発見しました。ダイス処理を開始します。");

        int currentTurnIndex = room.getTurnIndex();
        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

    if (currentPlayer.getId().equals(playerId) && currentPlayer.isSkipped()) {
        System.out.println("[Game] " + playerId + " は休みです。");
        currentPlayer.setSkipped(false); 

        int nextIdx = (currentTurnIndex + 1) % room.getPlayers().size();
        room.setTurnIndex(nextIdx);
        Player nextPlayer = room.getPlayers().get(nextIdx);

        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "GAME_UPDATE");
        response.put("diceValue", "休み"); 
        response.put("lastPlayerId", playerId);
        response.put("newPosition", currentPlayer.getCurrentPosition());
        response.put("earnedUnits", currentPlayer.getEarnedUnits());   
        response.put("expectedUnits", currentPlayer.getExpectedUnits());
        response.put("nextPlayerId", nextPlayer.getId());
        response.put("isGraduated", false);
        response.put("message", playerId + " は一回休みです。");

        broadcastToRoom(room, response);
        return;
    }

    if (!currentPlayer.getId().equals(playerId)) {
        System.out.println("[Game] 却下: " + playerId + " の番ではありません。現在の手番: " + currentPlayer.getId());
        return;
    }

        int rolledNumber = diceController.executeRoll(itemType, targetValue);
        System.out.println("[Game] ダイスの出目: " + rolledNumber);

        // 移動計算
        int oldPos = currentPlayer.getCurrentPosition();
        int tempPos = oldPos + rolledNumber;
        
        if (tempPos >= 20) {
            callCreditManager(currentPlayer, true); 
        }
        
        int newPos = tempPos % 20;
        currentPlayer.setCurrentPosition(newPos);

    GameEvent event = gameMap.getGameEvent(newPos);
    if (event != null) {
        // JavaScriptのアラートは使わず、System.out でサーバーにのみ表示
        System.out.println("[Event発生] プレイヤー: " + playerId + " | マス: " + newPos + " | 内容: " + event.getEventContent());
        
        // イベントによる単位の増減を適用
        int currentExpected = currentPlayer.getExpectedUnits();
        int adjustment = event.getCreditAdjustmentValue();
        currentPlayer.setExpectedUnits(currentExpected + adjustment);
        
        System.out.println("[Event適用] 予定単位が更新されました: " + currentExpected + " -> " + currentPlayer.getExpectedUnits());

        // 特殊効果（一回休みなど）のログ出力
        if (event.getEventEffect() == GameEvent.EFFECT_SKIP) {
            System.out.println("[Event効果] " + playerId + " は次の一回休みが適用されます。");
            currentPlayer.setSkipped(true);
            currentPlayer.setExpectedUnits(currentExpected + adjustment);
        }
    }

        // ターン送り
        int nextTurnIndex = (currentTurnIndex + 1) % room.getPlayers().size();
        room.setTurnIndex(nextTurnIndex);
        Player nextPlayer = room.getPlayers().get(nextTurnIndex);

        boolean isGraduated = currentPlayer.checkGraduationRequirement();

        // クライアント通知
        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "GAME_UPDATE");
        response.put("lastPlayerId", playerId);
        response.put("diceValue", rolledNumber);
        response.put("newPosition", currentPlayer.getCurrentPosition());
        response.put("earnedUnits", currentPlayer.getEarnedUnits());
        response.put("expectedUnits", currentPlayer.getExpectedUnits());
        response.put("nextPlayerId", nextPlayer.getId());
        response.put("isGraduated", isGraduated);

        System.out.println("[Game] 計算完了。ブラウザへ結果を送信します。");
        broadcastToRoom(room, response);
        
        if (isGraduated) {
            endGame(room);
        }
    }

    // 170行目付近の resetAFKCount も修正
    private void resetAFKCount(String roomId, String playerId) {
        if (roomId == null) return;
        // ✅ 修正：getRoomManager() を使う
        RoomManager rm = getRoomManager();
        if (rm == null) return;
        Room room = rm.getRoom(roomId);
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
            SessionManager.sendToUser(p.getId(), json);
        }
    }

    private void callCreditManager(Player player, boolean isLap) {
        if (isLap) {
            player.setEarnedUnits(player.getEarnedUnits() + player.getExpectedUnits());
            player.setExpectedUnits(25);
        }
    }

    private void endGame(Room room) {
        Map<String, Object> endMsg = new HashMap<>();
        endMsg.put("taskName", "GAME_END");
        broadcastToRoom(room, endMsg);
    }
}