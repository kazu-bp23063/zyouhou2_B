package com.example.application.ClientManagementServer;

import com.example.application.ClientManagementServer.Message.ClientMessage;
import com.example.application.ClientManagementServer.Message.ClientToClientManagementMessage;
import com.google.gson.Gson;
import jakarta.websocket.Session;

public class ClientManagementController {
    private final Gson gson = new Gson();
    private final AccountManagement management = new AccountManagement(); // ロジック共有
    private static final MatchingManagement matchingManagement = new MatchingManagement();

    public void processClientMessage(String json, Session session) {
        ClientToClientManagementMessage msg = gson.fromJson(json, ClientToClientManagementMessage.class);
        
        switch (msg.getTaskName()) {
            case "LOGIN" -> {
                User user = management.login(msg.getUserName(), msg.getPassword());
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
                matchingManagement.addUserToWaitList(session, msg.getUserName(), msg.getUserId());
            }
        }
    }

    private void send(Session session, Object obj) {
        try { session.getBasicRemote().sendText(gson.toJson(obj)); } catch (Exception e) {}
    }
}