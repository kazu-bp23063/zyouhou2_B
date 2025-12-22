package com.example.application.ClientManagementServer.Message;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*アプリケーションサーバ送信用メッセージクラス */
@Getter
@AllArgsConstructor
public class ApplicationMessage {
    private String taskName;
    private String matchId;
    private List<PlayerInfo> players;

    @Getter
    @AllArgsConstructor
    public static class PlayerInfo {
        private String userId;
        private String userName;
    }
}
