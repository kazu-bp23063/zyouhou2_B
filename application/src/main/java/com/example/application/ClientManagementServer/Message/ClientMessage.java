package com.example.application.ClientManagementServer.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/* クライアント送信用メッセージクラス */
@Getter
@AllArgsConstructor
public class ClientMessage {
    private boolean authenticationFlag;
    private GameRecord gameRecord;

    /* 戦績用内部クラス */
    @Getter
    @AllArgsConstructor
    public static class GameRecord {
        private int winCount;
        private int loseCount;
    }
}