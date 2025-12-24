package com.example.application.ClientManagementServer;

import com.example.application.Client.ClientToClientManagementMessage;
import com.example.application.ClientManagementServer.Message.ClientMessage;
import com.google.gson.Gson;

import jakarta.websocket.Session;

public class ClientManagementController {
    private final Gson gson = new Gson();
    private AccountManagement management;
    private static final MatchingManagement matchingManagement = new MatchingManagement();
    private CommunicationController communicationController = new CommunicationController();

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

    public void processApplicationMessage(String json, Session session) {
        ClientToClientManagementMessage msg = gson.fromJson(json, ClientToClientManagementMessage.class);
        System.out.println("[ClientManagementController] request: " + msg.getTaskName());
        switch (msg.getTaskName()) {
            case "MATCH_CREATED" -> handleMatchCreated(msg, session);
        }
    }

    private void handleLogin(ClientToClientManagementMessage msg, Session session) {
        boolean isAuthenticated = management.login(msg.getUserName(), msg.getPassword());
        ClientMessage clientMessage = new ClientMessage(isAuthenticated, new ClientMessage.GameRecord(0, 0));
        communicationController.sendMessage(session, gson.toJson(clientMessage));
    }

    private void handleRegister(ClientToClientManagementMessage msg, Session session) {
        boolean isRegistered = management.registerAccount(msg.getUserName(), msg.getPassword());
        ClientMessage clientMessage = new ClientMessage(isRegistered, new ClientMessage.GameRecord(0, 0));
        communicationController.sendMessage(session, gson.toJson(clientMessage));
    }

    private void handleLogout(ClientToClientManagementMessage msg, Session session) {
        boolean isLoggedOut = management.logout(msg.getUserName());
        ClientMessage clientMessage = new ClientMessage(isLoggedOut, null);
        communicationController.sendMessage(session, gson.toJson(clientMessage));
    }

    private void handleMatching(ClientToClientManagementMessage msg, Session session) {
        System.out.println("[HandleMatching] User " + msg.getUserName() + " requested matching.");
        matchingManagement.addUserToWaitList(session, msg.getUserName(), msg.getUserId());
    }

    private void handleMatchCreated(ClientToClientManagementMessage msg, Session session) {
        System.out.println("[HandleMatchCreated] Match created with ID: " + msg.getMatchId());
        communicationController.sendMessage(session, gson.toJson(msg));
    }
}
