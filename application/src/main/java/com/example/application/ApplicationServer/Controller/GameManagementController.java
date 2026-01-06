package com.example.application.ApplicationServer.Controller;

import java.util.HashMap;
import java.util.Map;
import com.example.application.ApplicationServer.Entity.*; // 必要に応じて
import com.google.gson.Gson;
import jakarta.websocket.Session;

public class GameManagementController {
    private final Gson gson = new Gson();
    private final DiceController diceController = new DiceController();
    private final GameMap gameMap = new GameMap(); // （三平）マスイベント参照用（追加）

    // ✅ RoomManager を安全に取得するメソッド
    private RoomManager getRoomManager() {
        return RoomManager.instance;
    }

// GameManagementController.java

public void processGameMessage(String json, Session session) {
    Map<String, Object> msg = gson.fromJson(json, Map.class);
    String taskName = (String) msg.get("taskName");
    String playerId = (String) msg.get("playerId");
    String roomId = (String) msg.get("roomId"); 

    if (playerId != null) {
        // ✅ 修正：コメントアウトを外し、セッションを登録する
        SessionManager.userSessions.put(playerId, session); 
        resetAFKCount(roomId, playerId);
        System.out.println("[Game] Session registered for: " + playerId);
    }

    switch (taskName) {
        case "GAME_JOIN" -> {
            // ✅ 修正：RoomManagerから部屋を取得し、プレイヤーをリストに追加する
            RoomManager rm = getRoomManager();
            Room room = rm.getRoom(roomId);
            if (room != null) {
                // 重複チェックをしてから追加
                boolean exists = room.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
                if (!exists) {
                    // 名前とIDを同じにして、仮の色(redなど)で追加
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
        // ✅ 部屋が見つからない場合にログを出すようにする
        System.out.println("[Game] 部屋が見つかりません: " + roomId);
        return;
    }

    System.out.println("[Game] 部屋を発見しました。ダイス処理を開始します。");

        int currentTurnIndex = room.getTurnIndex();
        Player currentPlayer = room.getPlayers().get(currentTurnIndex);

        // ✅ 手番チェックのログを追加
    if (!currentPlayer.getId().equals(playerId)) {
        System.out.println("[Game] 却下: " + playerId + " の番ではありません。現在の手番: " + currentPlayer.getId());
        return;
    }

        if (!currentPlayer.getId().equals(playerId)) return;

        // --- ダイス実行 ---
        int rolledNumber = diceController.executeRoll(itemType, targetValue);
        System.out.println("[Game] ダイスの出目: " + rolledNumber);

        // 移動計算
        int oldPos = currentPlayer.getCurrentPosition();
        int tempPos = oldPos + rolledNumber;
        
        if (tempPos >= 20) {
            callCreditManager(currentPlayer, true); 
        }
        
        int newPos = tempPos % 20;

        // （三平）ここで一旦「サイコロ分の着地位置」を保存（UIを「先に6マス移動」させるため）
        int landingPosition = newPos; // （三平）

        // （三平）サイコロ分だけまず反映（この後にイベント移動を別で反映）
        currentPlayer.setCurrentPosition(landingPosition); // （三平）

        // （三平）止まったマスのイベント取得（なければnull）
        GameEvent event = gameMap.getGameEvent(landingPosition); // （三平）
        String eventMessage = null; // （三平）画面ポップアップ用メッセージ

        // （三平）最終位置（イベント移動後）を別変数で持つ
        int finalPosition = landingPosition; // （三平）

        // （三平）2マス進む（EFFECT_BACK を「特殊移動イベント」の印として流用）
        // （三平）進んだ先のイベントは発動しない
        if (event != null && event.getEventEffect() == GameEvent.EFFECT_BACK) { // （三平）
            finalPosition = (landingPosition + 2) % 20; // （三平）
            eventMessage = event.getEventContent(); // （三平）
            event = null; // （三平）進んだ先のイベントは発動しない
        }

        // （三平）1マス戻る（マス6・14専用）
        // （三平）戻った先のイベントは発動しない
        if (event != null && (landingPosition == 6 || landingPosition == 14)) { // （三平）
            finalPosition = (landingPosition + 19) % 20; // （三平）-1を20マスで安全に処理
            eventMessage = event.getEventContent(); // （三平）
            event = null; // （三平）戻った先のイベントは発動しない
        }

        // （三平）通常イベント（+5 / -2 / 一回休み）の適用（位置は動かさない）
        if (event != null) { // （三平）
            // （三平）予定単位の増減（creditAdjustmentValueを予定単位に適用）
            int updatedExpected = currentPlayer.getExpectedUnits() + event.getCreditAdjustmentValue(); // （三平）
            currentPlayer.setExpectedUnits(Math.max(0, updatedExpected)); // （三平）

            // （三平）一回休み（Playerにフラグがある前提：isSkipped）
            if (event.getEventEffect() == GameEvent.EFFECT_SKIP) { // （三平）
                currentPlayer.setSkipped(true); // （三平）
            }

            // （三平）ポップアップ文言
            eventMessage = event.getEventContent(); // （三平）
        }

        // （三平）イベント移動（+2 / -1）がある場合はここで最終位置を反映
        currentPlayer.setCurrentPosition(finalPosition); // （三平）

        // ターン送り
        int nextTurnIndex = (currentTurnIndex + 1) % room.getPlayers().size();

        // （三平）一回休みの自動スキップ処理（次の手番決定時に isSkipped を確認して飛ばす）
        int guard = 0; // （三平）無限ループ防止
        while (room.getPlayers().get(nextTurnIndex).isSkipped()) { // （三平）
            Player skippedPlayer = room.getPlayers().get(nextTurnIndex); // （三平）
            skippedPlayer.setSkipped(false); // （三平）休みを消化したので戻す
            System.out.println("[Game] 一回休み発動: " + skippedPlayer.getId() + " をスキップします。"); // （三平）
            nextTurnIndex = (nextTurnIndex + 1) % room.getPlayers().size(); // （三平）
            guard++; // （三平）
            if (guard >= room.getPlayers().size()) break; // （三平）全員スキップの異常系対策
        }

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
        response.put("nextPlayerId", nextPlayer.getId());
        response.put("isGraduated", isGraduated);

        // （三平）イベント表示用メッセージ（nullなら何も表示しない）
        response.put("eventMessage", eventMessage); // （三平）
        // （三平）予定単位も画面で使うなら送る（必要なければ消してOK）
        response.put("expectedUnits", currentPlayer.getExpectedUnits()); // （三平）

        // （三平）「途中地点」と「最終地点」を両方返す（6マス移動→ポップアップ→イベント移動のため）
        response.put("landingPosition", landingPosition); // （三平）
        response.put("finalPosition", finalPosition); // （三平）

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
