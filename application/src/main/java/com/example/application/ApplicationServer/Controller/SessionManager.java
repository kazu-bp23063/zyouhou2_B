package com.example.application.ApplicationServer.Controller;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    public static final Map<String, Session> userSessions = new ConcurrentHashMap<>();

    public static void sendToUser(String playerId, String message) {
        Session session = userSessions.get(playerId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }
}