package com.example.application.ClientManagementServer;

import com.example.application.ApplicationServer.Controller.GameManagementController; // 追加
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/client-management")
public class CommunicationController {
    private static final ClientManagementController authController = new ClientManagementController();
    private static final GameManagementController gameController = new GameManagementController(); // 追加

    @OnMessage
    public void onMessage(final String json, final Session session) throws IOException {
        System.out.println("[Endpoint] Received: " + json);
        
        // メッセージの内容によってコントローラーを振り分ける
        if (json.contains("LOGIN") || json.contains("REGISTER") || json.contains("MATCHING")) {
            authController.processClientMessage(json, session);
        } else {
            gameController.processGameMessage(json, session); // ゲーム系の処理へ
        }
    }

    @OnOpen
    public void onOpen(Session session) { System.out.println("Connected: " + session.getId()); }

    @OnClose
    public void onClose(Session session) { System.out.println("Closed: " + session.getId()); }

    @OnError
    public void onError(Session session, Throwable error) { error.printStackTrace(); }
}