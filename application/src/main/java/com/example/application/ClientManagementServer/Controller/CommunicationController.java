package com.example.application.ClientManagementServer.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@Component
@ServerEndpoint("/matching")
public class CommunicationController {
    public static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private static final ClientManagementController authController = new ClientManagementController();

    @OnMessage
    public void onMessage(String json, Session session) {
        // 管理サーバーが担当するタスクのみを処理
        if (json.contains("LOGIN") || json.contains("REGISTER") || json.contains("MATCHING") || json.contains("LOGOUT")) {
            authController.processClientMessage(json, session);
            System.out.println("[Communication] 管理サーバーでゲームメッセージを処理しました。");
        } else {
            System.out.println("[Communication] 警告: 管理サーバーでゲームメッセージを受信しました。無視します。");
        }
    }
    
    public static void sendToUser(String userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            System.out.println("[Communication] Sending to " + userId + ": " + message);
            session.getAsyncRemote().sendText(message);
        }
    }
}