package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ClientManagementServer.CommunicationController;
import com.google.gson.Gson;
import jakarta.websocket.Session;
import java.util.HashMap;
import java.util.Map;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final RoomManager roomManager = new RoomManager();
    private final DiceController diceController = new DiceController();

    /**
     * WebSocket経由で届くゲーム内メッセージを処理する
     */
   public void processGameMessage(String json, Session session) {
    Map<String, Object> msg = gson.fromJson(json, Map.class);
    String taskName = (String) msg.get("taskName");
    String playerId = (String) msg.get("playerId");

    // 全てのメッセージにおいて、送信者のセッションを最新状態に更新する
    if (playerId != null) {
        CommunicationController.userSessions.put(playerId, session);
        System.out.println("[Game] Session registered for: " + playerId);
    }

    switch (taskName) {
        case "GAME_JOIN" -> {
            // 接続確認用のレスポンス（必要なら）
            System.out.println("[Game] Player " + playerId + " joined the game session.");
        }
        case "GAME_ROLL" -> handleRoll(msg);
    }
}

    /**
     * ダイスを振り、移動処理と単位加算を行い、全員に同期する
     */
    private void handleRoll(Map<String, Object> msg) {
        String roomId = (String) msg.get("roomId");
        String playerId = (String) msg.get("playerId");

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        // 1. ターンのチェック（現在の手番プレイヤーか確認）
        int currentTurnIndex = room.getTurnIndex();
        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

        if (!currentPlayer.getId().equals(playerId)) {
            System.out.println("[Game] 無効な手番操作: " + playerId);
            return;
        }

        // 2. ダイスを実行（DiceControllerを使用）
        int rolledNumber = diceController.roll();

        // 3. 移動と単位計算ロジック
        int oldPos = currentPlayer.getCurrentPosition(); // 現在位置を取得
        int newPos = (oldPos + rolledNumber) % 20;       // 全20マスの計算

        // 4. 一周（スタート地点：0番マスを通過）判定
        // CreditManager の graduateUnits ロジックに基づき、expectedUnitsを加算
        if (oldPos + rolledNumber >= 20) {
            int earned = currentPlayer.getEarnedUnits();
            int expected = currentPlayer.getExpectedUnits();
            currentPlayer.setEarnedUnits(earned + expected); // 取得単位を更新
            System.out.println("[Game] プレイヤー " + playerId + " がスタートを通過しました。単位獲得: " + expected);
        }
        
        currentPlayer.setCurrentPosition(newPos); // 新しい位置をセット

        // 5. ターンを次のプレイヤーに進める
        int nextTurnIndex = (currentTurnIndex + 1) % room.getPlayers().size();
        room.setTurnIndex(nextTurnIndex);
        Player nextPlayer = room.getPlayers().get(nextTurnIndex);

        // 6. 卒業判定（124単位以上で卒業）
        boolean isGraduated = currentPlayer.getEarnedUnits() >= 25;

        // 7. 全プレイヤーへリアルタイム通知（ブロードキャスト）
        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "GAME_UPDATE");
        response.put("lastPlayerId", playerId);    // 誰が振ったか
        response.put("diceValue", rolledNumber);  // 出目
        response.put("newPosition", newPos);      // 移動後の位置
        response.put("earnedUnits", currentPlayer.getEarnedUnits()); // 最新の単位
        response.put("nextPlayerId", nextPlayer.getId());   // 次に振る人
        response.put("isGraduated", isGraduated);           // 卒業したか

        broadcastToRoom(room, response);
    }

    /**
     * 指定されたルームの全プレイヤーにメッセージを一斉送信する
     */
    private void broadcastToRoom(Room room, Object messageObj) {
        String json = gson.toJson(messageObj);
        for (Player p : room.getPlayers()) {
            CommunicationController.sendToUser(p.getId(), json);
        }
    }
}