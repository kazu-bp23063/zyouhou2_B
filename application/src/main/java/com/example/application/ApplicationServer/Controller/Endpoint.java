package com.example.application.ApplicationServer.Controller;

import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/game-server") 
public class Endpoint {
    private static final Gson gson = new Gson();
    // 同じ ApplicationServer パッケージ内のコントローラを呼び出す
    private static final GameManagementController gameController = new GameManagementController();

    @OnMessage
    public void onMessage(String message, Session session) {
        // ゲーム進行（ROLL, JOIN）のメッセージだけをここで処理
        if (message.contains("GAME_JOIN") || message.contains("GAME_ROLL")) {
            gameController.processGameMessage(message, session);
        }
    }
}