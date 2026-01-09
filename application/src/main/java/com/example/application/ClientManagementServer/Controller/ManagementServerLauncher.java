package com.example.application.ClientManagementServer.Controller;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.server.Server;
import java.net.URI;

public class ManagementServerLauncher {
    static String contextRoot = "/app";
    static String protocol = "ws";
    static int port = 8080;
    // 物理IP 192.168.11.13 を許可するために 0.0.0.0 を指定
    public static final String restUri = "http://0.0.0.0:8082/api"; 

    public static void main(String[] args) throws Exception {
        // WebSocketサーバの起動
        Server wsServer = new Server(protocol, port, contextRoot, null, CommunicationController.class);
        wsServer.start();

        // REST APIサーバの起動（サンプルの書き方を物理分離用に調整）
        // ✅ 404エラーを防ぐため、AccountManagementクラスを明示的に登録
        final ResourceConfig rc = new ResourceConfig(AccountManagement.class);
        
        final HttpServer restServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(restUri), rc);

        try {
            wsServer.start();
            System.out.println("[Management Server] Started. REST: 8082 / WS: 8080");
            System.in.read(); 
        } finally {
            wsServer.stop();
            restServer.shutdownNow();
        }
    }
}