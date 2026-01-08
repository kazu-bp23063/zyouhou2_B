package com.example.application;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import jakarta.websocket.Session;
import jakarta.websocket.RemoteEndpoint;
import com.example.application.ClientManagementServer.Controller.ClientManagementController;

class ClientManagementControllerTests {

    @Test
    void testMatchingTask() throws Exception {
        // 1. 準備：コントローラーと偽のセッション(Mock)を作る
        ClientManagementController controller = new ClientManagementController();
        Session mockSession = mock(Session.class);
        RemoteEndpoint.Basic mockBasic = mock(RemoteEndpoint.Basic.class);

        // session.getBasicRemote() が呼ばれたら、偽の送信機を返すように設定
        when(mockSession.getBasicRemote()).thenReturn(mockBasic);

        // 2. 実行：マッチング要求のJSONを作成してメソッドを呼ぶ
        String json = "{\"taskName\":\"REGISTER\", \"userName\":\"testhiro\", \"userId\":\"user123\"}";
        controller.processClientMessage(json, mockSession);

        // 3. 検証：内部で意図した処理が動いたかログなどで確認
        // (MatchingManagementがstaticなので、副作用をSystem.out等で確認するか、
        //  引数が必要なメソッドが呼ばれたかを検証します)
    }
}