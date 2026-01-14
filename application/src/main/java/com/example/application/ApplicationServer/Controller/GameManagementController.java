package com.example.application.ApplicationServer.Controller;

import java.util.HashMap;
import java.util.Map;

import com.example.application.ApplicationServer.Entity.GameEvent;
import com.example.application.ApplicationServer.Entity.GameMap;
import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
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
        }

        System.out.println("[Game] Message received: " + taskName + " / Player: " + playerId + " / Room: " + roomId);

        switch (taskName) {
            case "GAME_JOIN" -> {
                RoomManager rm = getRoomManager();
                if (rm == null) {
                    System.out.println("[Game] Error: RoomManager is null.");
                    return;
                }
                Room room = rm.getRoom(roomId);
                if (room != null) {
                    boolean exists = room.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
                    if (!exists) {
                        Player newPlayer = new Player(playerId, "red");
                        newPlayer.setId(playerId);
                        room.getPlayers().add(newPlayer);
                        System.out.println("[Game] JOIN処理: 部屋 " + roomId + " にプレイヤー " + playerId + " を追加しました。(現在: " + room.getPlayers().size() + "人)");
                    } else {
                        System.out.println("[Game] JOIN処理: プレイヤー " + playerId + " は既に参加済みです。");
                    }
                } else {
                    System.out.println("[Game] JOINエラー: 部屋 " + roomId + " が見つかりません。");
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
        if (rm == null) return;
        Room room = rm.getRoom(roomId);
        
        if (room == null) {
            System.out.println("[Game] Rollエラー: 部屋が見つかりません ID=" + roomId);
            return;
        }

        // ★修正点: プレイヤーリストが空の場合の防御
        if (room.getPlayers().isEmpty()) {
            System.out.println("[Game] Rollエラー: 部屋 " + roomId + " のプレイヤーリストが空です。");
            return; // ここでリターンすることでクラッシュを防ぐ
        }

        int currentTurnIndex = room.getTurnIndex();
        
        // ★修正点: インデックス範囲外の防御
        if (currentTurnIndex < 0 || currentTurnIndex >= room.getPlayers().size()) {
            System.out.println("[Game] Rollエラー: turnIndex (" + currentTurnIndex + ") が不正です。プレイヤー人数: " + room.getPlayers().size());
            // 強制的に0に戻すなどの復旧処理
            room.setTurnIndex(0);
            currentTurnIndex = 0;
        }

        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

        if (!currentPlayer.getId().equals(playerId)) {
            // 順番補正ロジック
            int idx = -1;
            for (int i = 0; i < room.getPlayers().size(); i++) {
                if (room.getPlayers().get(i).getId().equals(playerId)) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                System.out.println("[Game] ターン補正: " + playerId + " に合わせて turnIndex=" + idx + " へ修正");
                room.setTurnIndex(idx);
                currentTurnIndex = idx;
                currentPlayer = room.getPlayers().get(idx);
            }
        }

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
            response.put("usedDouble", currentPlayer.isUsedDouble());
            response.put("usedJust", currentPlayer.isUsedJust());

            broadcastToRoom(room, response);
            return;
        }

        if (!currentPlayer.getId().equals(playerId)) {
            System.out.println("[Game] 却下: " + playerId + " の番ではありません。現在の手番ID: " + currentPlayer.getId());
            return;
        }

        // --- アイテム使用チェック ---
        if ("DOUBLE".equals(itemType)) {
            if (currentPlayer.isUsedDouble()) return;
            currentPlayer.setUsedDouble(true);
        } else if ("JUST".equals(itemType)) {
            if (currentPlayer.isUsedJust()) return;
            currentPlayer.setUsedJust(true);
        }

        // ダイス実行
        int rolledNumber = diceController.executeRoll(itemType, targetValue);
        System.out.println("[Game] ダイスの出目: " + rolledNumber);

        // 移動処理
        int oldPos = currentPlayer.getCurrentPosition();
        int tempPos = oldPos + rolledNumber;

        if (tempPos >= 20) {
            callCreditManager(currentPlayer, true);
        }

        int newPos = tempPos % 20;
        currentPlayer.setCurrentPosition(newPos);

        GameEvent event = gameMap.getGameEvent(newPos);
        if (event != null) {
            System.out.println("[Event] プレイヤー: " + playerId + " / マス: " + newPos + " / " + event.getEventContent());
            int currentExpected = currentPlayer.getExpectedUnits();
            int adjustment = event.getCreditAdjustmentValue();
            currentPlayer.setExpectedUnits(currentExpected + adjustment);

            if (event.getEventEffect() == GameEvent.EFFECT_SKIP) {
                currentPlayer.setSkipped(true);
                currentPlayer.setExpectedUnits(currentExpected + adjustment);
            }
            else if (event.getEventEffect() == GameEvent.EFFECT_RECOVERY) {
                currentPlayer.setUsedDouble(false);
                currentPlayer.setUsedJust(false);
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
        response.put("usedDouble", currentPlayer.isUsedDouble());
        response.put("usedJust", currentPlayer.isUsedJust());

        broadcastToRoom(room, response);

        if (isGraduated) {
            endGame(room);
        }
    }

    private void resetAFKCount(String roomId, String playerId) {
        if (roomId == null) return;
        RoomManager rm = getRoomManager();
        if (rm != null) {
            Room room = rm.getRoom(roomId);
            if (room != null && !room.getPlayers().isEmpty()) {
                room.getPlayers().stream()
                        .filter(p -> p.getId().equals(playerId))
                        .findFirst()
                        .ifPresent(p -> p.setAfkCount(0));
            }
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