package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Message.ApplicationMessage;
import com.example.application.ApplicationServer.Message.ApplicationToClientManagementMessage;
import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.UUID;

@ServerEndpoint("/application")
public class EndpointSample {
    private static final Gson gson = new Gson();

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("[ApplicationServer] Received: " + message);
        ApplicationMessage req = gson.fromJson(message, ApplicationMessage.class);

        if ("CREATE_ROOM".equals(req.getTaskName())) {
            String roomId = "room-" + UUID.randomUUID().toString().substring(0, 8);
            
            ApplicationToClientManagementMessage res = new ApplicationToClientManagementMessage(
                "ROOM_CREATED",
                req.getMatchId(),
                roomId
            );

            session.getAsyncRemote().sendText(gson.toJson(res));
        }
    }
}