package com.example.application;

import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/game")
public class ApplicationCommunicator { 

    // 接続している全ユーザーのセッション管理
    private static Set<Session> establishedSessions
            = Collections.synchronizedSet(new HashSet<Session>());

    // JSON変換用
    static Gson gson = new Gson();

    //RoomManagerをスタティックで共有（サーバ上で一つ）
    private static RoomManager roomManager = new RoomManager();

    @OnOpen
    public void establishConnection(Session session, EndpointConfig ec) {
        establishedSessions.add(session);
        System.out.println("[AppCommunicator] 接続開始 ID:" + session.getId());
        
    }

    @OnClose
    public void closeConnection(Session session) {
        System.out.println("[AppCommunicator] 接続終了 ID:" + session.getId());
        establishedSessions.remove(session);
        
        // 接続が切れたことをRoomManager等に通知する必要があればここに書く
    }

    @OnMessage
    public void receiveData(final String message, final Session session) throws IOException {
        System.out.println("[AppCommunicator] 受信: " + message);

        try {
            // 1. JSON (String) を オブジェクト (ClientToApplication) に変換
            // ※ ClientToApplicationクラスをまだ作っていない場合はエラーになるので、
            //    後で作る必要があります。
            ClientToApplication request = gson.fromJson(message, ClientToApplication.class);

            // 2. RoomManagerに処理を依頼する
            // どの部屋の操作か、誰の操作かなどの情報を渡す必要があります
            // roomManager.executeOperation(request, session); のようなイメージ
            
            // ★今はまだRoomManagerの中身がないので、とりあえずオウム返しだけしておきます
            sendData(session, "サーバーが受信しました: " + message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("JSON変換エラーまたは処理エラー");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("[AppCommunicator] エラー発生 ID:" + session.getId());
        error.printStackTrace();
    }

    public void sendData(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*  sendBroadcastDataいるかわからんけど一応
    public void sendBroadcastData(String message) {
        establishedSessions.forEach(session -> {
            sendData(session, message);
        });
    }
    */
}