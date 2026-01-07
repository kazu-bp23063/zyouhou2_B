package com.example.application.Client.Controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.application.Client.Entity.ClientToClientManagementMessage;
import com.google.gson.Gson;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ClientEndpoint

public class ClientCommunicater implements Runnable {
    // タスク番号、送られてきた際は初期値の0のまま(林)
    private int taskNum = 10;
    // Json化したタスクで使う情報
    private String task;
    // 接続が確立したときに手に入るオブジェクト、通信パイプの役割(林)
    Session session;
    // 通信を行うためのコンテナ
    static WebSocketContainer container;
    // 接続先(林)
    static String serverEndpoint = "ws://localhost:8080/app/client-management";
    static int id = 1;
    // クラスのフィールドをjsonにするためのインスタンス
    static Gson gson = new Gson();
    static int sampleIncrement = 0;

    public static void main(String[] args) {
        ClientCommunicater client = new ClientCommunicater();

        Thread thread = new Thread(client, "ClientCommunicater");
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(client::closeConnection));

        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            client.closeConnection();
        }
    }

    // 接続を確立するメソッド
    public boolean establishConnection() {
        // 通信を行うためのエンジンを取得(林)
        container = ContainerProvider.getWebSocketContainer();
        try {
            // 設定したエンドポイントに接続をする
            session = container.connectToServer(this, URI.create(serverEndpoint));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void closeConnection() {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void receiveData(String data) {
        // 受け取ったJsonの情報をClientToClientManagementMessageクラスのフィールドにセット(林)
        try {
            ClientToClientManagementMessage msg = gson.fromJson(data, ClientToClientManagementMessage.class);
            this.task = msg.getTaskName();
            System.out.println("Received message: " + data);
        } catch (Exception e) {
            System.out.println("メッセージの受信に失敗しました。");
        }
    }

    public void sendData(String data) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("セッションが確立していません。");
        }
    }

    @Override
    public void run() {
        if (!establishConnection()) {
            System.out.println("サーバーへの接続に失敗しました。");
            return;
        }

        while (session != null && session.isOpen()) {
            try {
                // タスク情報を送信
                ClientToClientManagementMessage message = new ClientToClientManagementMessage(
                        "LOGIN",
                        null,
                        null,
                        null);
                String jsonMessage = gson.toJson(message);
                sendData(jsonMessage);
                // sampleIncrement++;
                // 5秒待機
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

    }

}