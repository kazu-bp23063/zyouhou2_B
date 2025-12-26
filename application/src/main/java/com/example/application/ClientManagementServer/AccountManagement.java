package com.example.application.ClientManagementServer;

import java.util.UUID;

public class AccountManagement {
    private final DatabaseAccess dbAccess;

    public AccountManagement() {
        this.dbAccess = new DatabaseAccess();
    }

    /**
     * ログイン処理
     * @return 認証成功かつ二重ログインでない場合に true
     */
    public boolean login(String username, String password) {
        // 1. ユーザー情報の取得
        User user = dbAccess.getUserByUsername(username);
        if (user == null) {
            System.out.println("[AccountManagement] Login failed: User not found -> " + username);
            return false;
        }

        // 2. パスワード照合
        if (!user.password().equals(password)) {
            System.out.println("[AccountManagement] Login failed: Wrong password -> " + username);
            return false;
        }

        // 3. 二重ログインチェック
        if (dbAccess.getLoginStatusByUsername(username)) {
            System.out.println("[AccountManagement] Login failed: Already logged in -> " + username);
            return false;
        }

        // 4. ログイン状態を「真」に更新
        return dbAccess.setLoginStatus(username, true);
    }

    /**
     * アカウント登録処理
     * @return 登録成功時に true
     */
    public boolean registerAccount(String username, String password) {
        // 1. ユーザー重複チェック
        if (dbAccess.getUserByUsername(username) != null) {
            System.out.println("[AccountManagement] Register failed: User already exists -> " + username);
            return false;
        }

        // 2. 新規ID（UUID）の発行と保存
        String newId = UUID.randomUUID().toString();
        User newUser = dbAccess.createUser(username, password, newId);
        
        return newUser != null;
    }

    /**
     * ログアウト処理
     * @return ログアウト成功時に true
     */
    public boolean logout(String username) {
        System.out.println("[AccountManagement] Logging out user -> " + username);
        return dbAccess.setLoginStatus(username, false);
    }
}