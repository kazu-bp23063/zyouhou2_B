package com.example.application.ClientManagementServer;

import com.example.application.ClientManagementServer.Message.ClientMessage;
import com.example.application.ClientManagementServer.Message.ApplicationToClientManagementMessage;
import com.google.gson.Gson;
import com.example.application.Client.Entity.ClientToClientManagementMessage;
import com.example.application.ClientManagementServer.AccountManagement;
import jakarta.websocket.Session;
import java.io.IOException;

public class ClientManagementController {
    private final Gson gson = new Gson();
    private AccountManagement management;
    private static final MatchingManagement matchingManagement = new MatchingManagement();

    // 通信用のインスタンス生成を削除！

    public ClientManagementController() {
        this.management = new AccountManagement();
    }

    public void processClientMessage(String json, Session session) {
        ClientToClientManagementMessage msg = gson.fromJson(json, ClientToClientManagementMessage.class);
        System.out.println("[ClientManagementController] request: " + msg.getTaskName());
        switch (msg.getTaskName()) {
            case "LOGIN" -> handleLogin(msg, session);
            case "REGISTER" -> handleRegister(msg, session);
            case "LOGOUT" -> handleLogout(msg, session);
            case "MATCHING" -> handleMatching(msg, session);
        }
    }

    // 共通の送信メソッド（sessionを使って直接送る）
    private void send(Session session, Object messageObj) {
        try {
            session.getBasicRemote().sendText(gson.toJson(messageObj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(ClientToClientManagementMessage msg, Session session) {
        boolean isAuthenticated = management.login(msg.getUserName(), msg.getPassword());
        ClientMessage clientMessage = new ClientMessage(isAuthenticated, new ClientMessage.GameRecord(0, 0));
        send(session, clientMessage); // 修正
    }

    private void handleRegister(ClientToClientManagementMessage msg, Session session) {
        boolean isRegistered = management.registerAccount(msg.getUserName(), msg.getPassword());
        ClientMessage clientMessage = new ClientMessage(isRegistered, new ClientMessage.GameRecord(0, 0));
        send(session, clientMessage); // 修正
    }

    private void handleLogout(ClientToClientManagementMessage msg, Session session) {
        boolean isLoggedOut = management.logout(msg.getUserName());
        ClientMessage clientMessage = new ClientMessage(isLoggedOut, null);
        send(session, clientMessage); // 修正
    }

   private void handleMatching(ClientToClientManagementMessage msg, Session session) {
    // ユーザー名が null または空の場合は無視する
    if (msg.getUserName() == null || msg.getUserName().isEmpty()) {
        System.out.println("[HandleMatching] Skipped null user request.");
        return;
    }
    System.out.println("[HandleMatching] User " + msg.getUserName() + " requested matching.");
    matchingManagement.addUserToWaitList(session, msg.getUserName(), msg.getUserId());
}

    private void handleMatchCreated(ApplicationToClientManagementMessage msg, Session session) {
        System.out.println("[HandleMatchCreated] Match created with ID: " + msg.getMatchId());
        send(session, msg); // 修正
    }
}