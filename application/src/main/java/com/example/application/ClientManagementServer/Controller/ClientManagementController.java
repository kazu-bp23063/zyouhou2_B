package com.example.application.ClientManagementServer.Controller;

import com.example.application.ClientManagementServer.Entity.User;
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
                System.out.println("[ClientManagementController] Login attempt for user: " + msg.getUserName() + " - " + (user != null ? "Success" : "Failure"));
            }
            case "REGISTER" -> {
                boolean res = management.registerAccount(msg.getUserName(), msg.getPassword());
                send(session, new ClientMessage(res, null));
                System.out.println("[ClientManagementController] Registration attempt for user: " + msg.getUserName() + " - " + (res ? "Success" : "Failure"));
            }
            case "LOGOUT" -> {
                management.logout(msg.getUserName());
                send(session, new ClientMessage(true, null));
                System.out.println("[ClientManagementController] Logout for user: " + msg.getUserName());
            }
            case "MATCHING" -> {
                matchingManagement.addUserToWaitList(session, msg.getUserName(), msg.getUserId());
                System.out.println("[ClientManagementController] Matching request for user: " + msg.getUserName());
            }
        }
    }

    private void send(Session session, Object obj) {
        try { 
            session.getBasicRemote().sendText(gson.toJson(obj)); 
            System.out.println("[ClientManagementController] Sent message to client: " + gson.toJson(obj));
        } catch (Exception e) {
            System.out.println("[ClientManagementController] Failed to send message to client: " + e.getMessage());
        }
    }
}