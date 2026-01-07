package com.example.application.ApplicationServer.Controller;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // 変数名を GameManagementController と合わせる
    public static final Map<String, Session> userSessions = new ConcurrentHashMap<>();

    public static void sendToUser(String userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            // 非同期でメッセージを送信
            session.getAsyncRemote().sendText(message);
            System.out.println("[SessionManager] Sent to " + userId + ": " + message);
        }
    }
}