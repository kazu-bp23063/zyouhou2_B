package com.example.application.ClientManagementServer;

import java.net.URI;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint
public class ApplicationServerClient {
    private Session session;
    private final WebSocketContainer container;
    private final URI uri;
    private static final String APP_SERVER_URI = "ws://localhost:8025/app/application";
    private final MatchingManagement management;

    public ApplicationServerClient(MatchingManagement management) {
        this.management = management;
        this.container = ContainerProvider.getWebSocketContainer();
        this.uri = URI.create(APP_SERVER_URI);
    }

    /* 未接続、または切断済みなら再接続する */
    private synchronized void ensureConnected() {
        if (session != null && session.isOpen()) {
            return;
        }
        System.out.println("[AppClient] connect(): " + uri);
        try {
            session = container.connectToServer(this, uri);
            System.out.println("[AppClient] session: " + session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) {
        System.out.println("[AppClient] sendMessage: " + msg);
        ensureConnected();
        session.getAsyncRemote().sendText(msg);
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[AppClient] onOpen: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[AppClient] Received: " + message);
        management.onRoomCreated(message);
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("[client] onError");
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[client] onClose: " + session.getId());
    }
}
