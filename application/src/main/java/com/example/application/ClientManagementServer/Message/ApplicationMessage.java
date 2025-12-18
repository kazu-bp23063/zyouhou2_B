package com.example.application.ClientManagementServer.Message;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*アプリケーションサーバ送信用メッセージクラス */
@Getter
@AllArgsConstructor
public class ApplicationMessage {
    private String taskName;
    private List<UserInfo> users;

    /* 内部ユーザー情報クラス */
    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private int userId;
        private String userName;
    }
}
