package com.example.application.ClientManagementServer.Controller;

import org.glassfish.tyrus.server.Server;

/**
 * Tyrusサーバーを使用して、管理サーバー側のWebSocketエンドポイントを起動するメインクラス。
 */
public class WebSocketClientManagementServer {
    // サーバーの基本設定
    static String contextRoot = "/app"; // コンテキストルート
    static String protocol = "ws";      // プロトコル
    static int port = 8080;             // ポート番号

    public static void main(String[] args) throws Exception {
        // サーバーインスタンスの生成。CommunicationControllerをエンドポイントとして登録
        Server server = new Server(protocol, port, contextRoot, null,
                CommunicationController.class);
        System.out.println("server: " + server);

        try {
            // サーバーを開始。キー入力があるまで待機し続ける
            server.start();
            System.in.read();
        } finally {
            // 終了時にサーバーを停止
            server.stop();
        }
    }
}