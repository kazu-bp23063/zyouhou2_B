package com.example.application.ClientManagementServer.Controller;

import java.util.*;

import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ApplicationServer.Controller.RoomManager;
import com.example.application.ApplicationServer.Entity.Player;
import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.AllArgsConstructor;

public class MatchingManagement {
    private static Deque<PlayerEntry> matchingWaitList = new ArrayDeque<>();
    private static final RoomManager roomManager = new RoomManager(); 
    private final Gson gson = new Gson();

    public synchronized void addUserToWaitList(Session session, String userName, String userId) {
        if (userName == null || userName.isEmpty()) return;

        boolean exists = matchingWaitList.stream().anyMatch(p -> p.userId.equals(userId));
        if (!exists) {
            matchingWaitList.addLast(new PlayerEntry(userName, userId, session));
        }

        System.out.println("[Matching] Player " + userName + " joined. size: " + matchingWaitList.size());

        if (matchingWaitList.size() >= 4) {
            List<PlayerEntry> group = new ArrayList<>();
            for (int i = 0; i < 4; i++) group.add(matchingWaitList.removeFirst());
            createUnifiedRoom(group);
        } else {
      
            broadcastWaitStatus();
        }
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

    private void createUnifiedRoom(List<PlayerEntry> group) {
        Room room = roomManager.createRoom();

        String[] colors = {"#ff4d4d", "#4d94ff", "#4dff88", "#ffdb4d"};
        for (int i = 0; i < group.size(); i++) {
            PlayerEntry entry = group.get(i);
            Player p = new Player(entry.userName, colors[i]);
            p.setId(entry.userId); 
            room.addPlayer(p);
            System.out.println("[Matching] Added player " + p.getName() + " to room " + room.getRoomId() + " with color " + colors[i]);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("taskName", "MATCH_FOUND");
        response.put("roomId", room.getRoomId()); 
        
        String json = gson.toJson(response);
        for (PlayerEntry player : group) {
            sendMessage(player.session, json);
            System.out.println("[Matching] Sent to " + player.userName + ": " + json);
        }
        System.out.println("[Matching] Match Success! RoomID: " + room.getRoomId());
    }

    private void sendMessage(Session session, String text) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(text);
                System.out.println("[Matching] Sent message to session: " + text);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[Matching] Failed to send message to session: " + e.getMessage());
        }
    }

    @AllArgsConstructor
    private static class PlayerEntry {
        private String userName;
        private String userId;
        private Session session;
    }
}