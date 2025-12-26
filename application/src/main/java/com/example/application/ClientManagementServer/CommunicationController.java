package com.example.application.ClientManagementServer;
import com.example.application.ApplicationServer.Controller.GameManagementController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@Component
@ServerEndpoint("/client-management")
public class CommunicationController {
    // 全ユーザーのセッションを管理するマップ（名前 -> セッション）
    public static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    private static final ClientManagementController authController = new ClientManagementController();
    private static final GameManagementController gameController = new GameManagementController();

    @OnMessage
    public void onMessage(String json, Session session) {
        // メッセージが届くたびに、そのユーザー名とセッションを紐付ける（簡略化のため）
        // ※本来はログイン成功時に行うのがベストです
        if (json.contains("\"userId\":\"")) {
            // JSONから強引に名前を抜くか、メッセージクラスにuserIdがある前提
            // ここではメッセージ処理の中で、gameControllerにsessionを渡すことで対応
        }
        
        if (json.contains("LOGIN") || json.contains("REGISTER") || json.contains("MATCHING")) {
            authController.processClientMessage(json, session);
        } else {
            gameController.processGameMessage(json, session);
        }
    }
    
    // 特定のユーザーにメッセージを送るための静的メソッド
    public static void sendToUser(String userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }
}