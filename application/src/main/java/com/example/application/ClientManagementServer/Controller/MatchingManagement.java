package com.example.application.ClientManagementServer.Controller;

import java.util.*;
import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 【管理サーバ(PC-A)専用】マッチング管理クラス
 * アプリケーションサーバのクラス(RoomManager等)への依存を完全に排除しました。
 */
public class MatchingManagement {
    // 待機リスト
    private static Deque<PlayerEntry> matchingWaitList = new ArrayDeque<>();
    private static Map<String, LocalRoom> activeRooms = new HashMap<>();
    private final Gson gson = new Gson();

    public synchronized void addUserToWaitList(Session session, String userName, String userId) {
        if (userName == null || userName.isEmpty()) return;

        boolean exists = matchingWaitList.stream().anyMatch(p -> p.userId.equals(userId));
        if (!exists) {
            matchingWaitList.addLast(new PlayerEntry(userName, userId, session));
        }

        System.out.println("[Matching] Player " + userName + " joined. size: " + matchingWaitList.size());

        // 4人揃ったらマッチング成立
        if (matchingWaitList.size() >= 4) {
            List<PlayerEntry> group = new ArrayList<>();
            for (int i = 0; i < 4; i++) group.add(matchingWaitList.removeFirst());
            createUnifiedRoom(group);
        } else {
            broadcastWaitStatus();
        }
    }

    private void createUnifiedRoom(List<PlayerEntry> group) {
        String roomId = Integer.toHexString(new Random().nextInt(0x10000)).toUpperCase();
        LocalRoom room = new LocalRoom(roomId);
        sendRoomToAppServer(room); // アプリサーバへ部屋情報を送信

    

        String[] colors = {"#ff4d4d", "#4d94ff", "#4dff88", "#ffdb4d"};
        for (int i = 0; i < group.size(); i++) {
            PlayerEntry entry = group.get(i);
            LocalPlayer p = new LocalPlayer(entry.userId, entry.userName, colors[i], 0, 0, 25);
            room.players.add(p);
        }
        activeRooms.put(roomId, room);

        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "MATCH_FOUND");
        response.put("roomId", roomId);

        String json = gson.toJson(response);
        for (PlayerEntry player : group) {
            sendMessage(player.session, json);
        }
        System.out.println("[Matching] Match Success! RoomID: " + roomId);
    }

    private void sendRoomToAppServer(LocalRoom room) {
        try {
            String appBase = getProp("app.server.rest.base", "http://localhost:8081/api");
            String appServerUrl = appBase + "/matching/register-room";
            System.out.println("[Management] Sending room info to App Server at " + appServerUrl);

            // HttpClient または RestTemplate を使って送信
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(appServerUrl))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(gson.toJson(room)))
                    .build();

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("[Management] アプリサーバーへ部屋情報を送信しました。");
        } catch (Exception e) {
            System.out.println("[Management] アプリサーバーへの送信に失敗: " + e.getMessage());
        }
    }

    private String getProp(String key, String def) {
        String env = System.getenv(key.toUpperCase().replace('.', '_').replace('-', '_'));
        if (env != null && !env.isBlank())
            return env;
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank())
            return sys;
        return def;
    }

    public LocalRoom getRoomById(String roomId) {
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
        try { if (session.isOpen()) session.getBasicRemote().sendText(text); } 
        catch (Exception e) { e.printStackTrace(); }
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
        public LocalRoom(String id) { this.roomId = id; }
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