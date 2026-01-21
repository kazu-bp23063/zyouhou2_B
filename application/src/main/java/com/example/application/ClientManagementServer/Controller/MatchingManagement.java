package com.example.application.ClientManagementServer.Controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 【管理サーバ専用】マッチング管理クラス
 * アプリケーションサーバのクラス(RoomManager等)への依存を完全に排除しました。
 */
public class MatchingManagement {
    // 待機リスト
    private static Deque<PlayerEntry> matchingWaitList = new ArrayDeque<>();
    private static Map<String, LocalRoom> activeRooms = new HashMap<>();
    private final Gson gson = new Gson();

    public synchronized void addUserToWaitList(Session session, String userName, String userId) {
        if (userName == null || userName.isEmpty())
            return;

        boolean exists = matchingWaitList.stream().anyMatch(p -> p.userId.equals(userId));
        if (!exists) {
            matchingWaitList.addLast(new PlayerEntry(userName, userId, session));
        }

        System.out.println("[Matching] Player " + userName + " joined. size: " + matchingWaitList.size());

        // 4人揃ったらマッチング成立
        if (matchingWaitList.size() >= 4) {
            List<PlayerEntry> group = new ArrayList<>();
            for (int i = 0; i < 4; i++)
                group.add(matchingWaitList.removeFirst());
            createUnifiedRoom(group);
        } else {
            broadcastWaitStatus();
        }
    }

    public synchronized void removeUserFromWaitList(String userId) {
        if (userId == null || userId.isEmpty())
            return;

        boolean removed = matchingWaitList.removeIf(p -> p.userId.equals(userId));
        if (removed) {
            System.out.println("[Matching] Player " + userId + " cancelled. size: " + matchingWaitList.size());
            broadcastWaitStatus();
        }
    }

    private void createUnifiedRoom(List<PlayerEntry> group) {
        String roomId = Integer.toHexString(new Random().nextInt(0x10000)).toUpperCase();
        LocalRoom room = new LocalRoom(roomId);

        // 先にプレイヤーリストを作成して追加する
        String[] colors = { "#ff4d4d", "#4d94ff", "#4dff88", "#ffdb4d" };
        for (int i = 0; i < group.size(); i++) {
            PlayerEntry entry = group.get(i);
            LocalPlayer p = new LocalPlayer(entry.userId, entry.userName, colors[i], 0, 0, 25);
            room.players.add(p);
        }

        // AppServerへの送信を行い、成功を確認してからクライアントへ通知する
        boolean success = sendRoomToAppServerSync(room);

        if (success) {
            activeRooms.put(roomId, room);

            Map<String, Object> response = new HashMap<>();
            response.put("taskName", "MATCH_FOUND");
            response.put("roomId", roomId);

            String json = gson.toJson(response);
            for (PlayerEntry player : group) {
                sendMessage(player.session, json);
            }
            System.out.println("[Matching] Match Success! RoomID: " + roomId + " (AppServer同期済み)");
        } else {
            System.out.println("[Matching] エラー: AppServerへの部屋登録に失敗したため、マッチングをキャンセルします。");
            // エラー時はユーザーをリストに戻すなどの処理が必要ですが、ひとまずログ出力のみ
        }
    }

    // 同期的に送信し、結果を確認するメソッド
    private boolean sendRoomToAppServerSync(LocalRoom room) {
        try {
            String appBase = "http://172.31.108.165:8081/api"; 
            String appServerUrl = appBase + "/matching/register-room";

            System.out.println("[Management] Sending room info to App Server at " + appServerUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appServerUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(room)))
                    .build();

            // sendAsyncではなくsendを使って完了を待つ
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("[Management] アプリサーバーへの登録成功: " + response.body());
                return true;
            } else {
                System.out.println(
                        "[Management] アプリサーバーへの登録失敗: Status=" + response.statusCode() + " Body=" + response.body());
                return false;
            }

        } catch (Exception e) {
            System.out.println("[Management] アプリサーバーへの送信例外: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ★修正箇所: static を追加しました
    public static LocalRoom getRoomById(String roomId) {
        return activeRooms.get(roomId);
    }

    private void broadcastWaitStatus() {
        List<String> userNames = matchingWaitList.stream().map(p -> p.userName).toList();
        Map<String, Object> statusMsg = new HashMap<>();
        statusMsg.put("taskName", "WAIT_STATUS");
        statusMsg.put("players", userNames);
        String json = gson.toJson(statusMsg);
        for (PlayerEntry entry : matchingWaitList) {
            sendMessage(entry.session, json);
        }
    }

    private void sendMessage(Session session, String text) {
        try {
            if (session.isOpen())
                session.getBasicRemote().sendText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    private static class PlayerEntry {
        String userName;
        String userId;
        Session session;
    }

    @Data
    public static class LocalRoom {
        private String roomId;
        private List<LocalPlayer> players = new ArrayList<>();
        private int turnIndex = 0;

        public LocalRoom(String id) {
            this.roomId = id;
        }
    }

    @Data
    @AllArgsConstructor
    public static class LocalPlayer {
        private String id;
        private String name;
        private String color;
        private int currentPosition = 0;
        private int earnedUnits = 0;
        private int expectedUnits = 25;
    }
}