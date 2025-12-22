package com.example.application.ClientManagementServer;

import org.glassfish.tyrus.server.Server;

public class WebSocketClientManagementServer {
    static String contextRoot = "/app";
    static String protocol = "ws";
    static int port = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server(protocol, port, contextRoot, null,
                CommunicationController.class);
        System.out.println("server: " + server);

        try {
            server.start();
            System.in.read();
        } finally {
            server.stop();
        }
    }
}