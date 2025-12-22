package com.example.application.ClientManagementServer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/*クライアント管理サーバ用エンドポイント */
@ServerEndpoint("/client-management")
public class CommunicationController {
    private static Set<Session> establishedSessions = Collections.synchronizedSet(new HashSet<Session>());
    private static final ClientManagementController controller = new ClientManagementController();

    static Gson gson = new Gson();

    /* セッションがオープンしたタイミングでセッションをセッション管理リストに追加 */
    @OnOpen
    public void OnOpen(Session session, EndpointConfig ec) {
        establishedSessions.add(session);
        System.out.println("[ClientManagementEndpoint] onOpen:" + session.getId());
    }

    /* 受信したメッセージをコントローラに渡して処理して返信を送信 */
    @OnMessage
    public void onMessage(final String json, final Session session) throws IOException {
        System.out.println("[ClientManagementEndpoint] onMessage: " + json);
        controller.processClientMessage(json, session);
    }

    /* セッションが閉じたらリストから削除 */
    @OnClose
    public void onClose(Session session) {
        System.out.println("[ClientManagementEndpoint] onClose:" + session.getId());
        establishedSessions.remove(session);
    }

    /* エラー発生時のログ出力 */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("[ClientManagementEndpoint] onError:" + session.getId());
        error.printStackTrace();
    }

    /* 指定したセッションにメッセージを送信 */
    public void sendMessage(Session session, String message) {
        System.out.println("[ClientManagementEndpoint] sendMessage(): " + message);
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 登録済み セッションすべてへブロードキャスト */
    public void sendBroadcastMessage(String message) {
        System.out.println("[WebSocketServerSample] sendBroadcastMessage(): " + message);
        establishedSessions.forEach(session -> {
            session.getAsyncRemote().sendText(message);
        });
    }
}
