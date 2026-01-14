package com.example.application.ClientManagementServer.Controller;

import com.example.application.ClientManagementServer.Entity.User;
import com.example.application.ClientManagementServer.Message.ClientMessage;
import com.example.application.ClientManagementServer.Message.ClientToClientManagementMessage;
import com.google.gson.Gson;
import jakarta.websocket.Session;

/**
 * WebSocket通信を制御するコントローラー
 */
public class ClientManagementController {
    private final Gson gson = new Gson();
    // ロジックを共有するためにインスタンス化
    private final AccountManagement management = new AccountManagement(); 
    private static final MatchingManagement matchingManagement = new MatchingManagement();

    public void processClientMessage(String json, Session session) {
        ClientToClientManagementMessage msg = gson.fromJson(json, ClientToClientManagementMessage.class);
        
        switch (msg.getTaskName()) {
            case "LOGIN" -> {
                // authenticate() メソッドを呼び出すように変更
                User user = management.authenticate(msg.getUserName(), msg.getPassword());
                send(session, new ClientMessage(user != null, null));
            }
            case "REGISTER" -> {
                boolean res = management.registerAccount(msg.getUserName(), msg.getPassword());
                send(session, new ClientMessage(res, null));
            }
            case "LOGOUT" -> {
                management.logout(msg.getUserName());
                send(session, new ClientMessage(true, null));
            }
            case "MATCHING" -> {
                // マッチング待機リストへ追加
                matchingManagement.addUserToWaitList(session, msg.getUserName(), msg.getUserId());
            }
            case "MATCHING_CANCEL" -> {
                // マッチング待機リストから削除
                matchingManagement.removeUserFromWaitList(msg.getUserId());
            }
        }
    }

    private void send(Session session, Object obj) {
        try { 
            session.getBasicRemote().sendText(gson.toJson(obj)); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}