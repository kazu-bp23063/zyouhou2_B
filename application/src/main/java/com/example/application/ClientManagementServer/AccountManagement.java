package com.example.application.ClientManagementServer;

import java.util.UUID;

public class AccountManagement {
    private final DatabaseAccess dbAccess;

    public AccountManagement() {
        this.dbAccess = new DatabaseAccess();
    }

    /**
     * ログイン処理
     * @return ログイン成功時にtrue
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            System.out.println("[AccountManagement] Loggin failed: Input is null");
            return false;
        }

        try {
            // ユーザー存在確認
            User user = dbAccess.getUserByUsername(username);
            if (user == null) {
                System.out.println("[AccountManagement] Login failed: User not found -> " + username);
                return false;
            }

            // パスワード照合
            if (!user.password().equals(password)) {
                System.out.println("[AccountManagement] Login failed: Wrong password -> " + username);
                return false;
            }

            // 二重ログインチェック
            if (dbAccess.getLoginStatusByUsername(username)) {
                System.out.println("[AccountManagement] Login failed: Already logged in -> " + username);
                return false;
            }

            // ログイン状態を更新
            boolean result = dbAccess.setLoginStatus(username, true);
            if (result) {
                System.out.println("[AccountManagement] Login success -> " + username);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * アカウント登録処理
     * @return 登録成功時にtrue
     */
    public boolean registerAccount(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        try {
            // ユーザー名の重複チェック
            if (dbAccess.getUserByUsername(username) != null) {
                System.out.println("[AccountManagement] Register failed: User already exists -> " + username);
                return false;
            }
            // IDの生成
            String newId = UUID.randomUUID().toString();
            // ユーザー作成
            User newUser = dbAccess.createUser(username, password, newId);

            boolean success = (newUser != null);
            if (success) {
                System.out.println("[AccountManagement] Register success -> " + username + " (ID: " + newId + ")");
            }
            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ログアウト処理
     * @return ログアウト成功時にtrue
     */
    public boolean logout(String username) {
        if (username == null) return false;

        try {
            System.out.println("[AccountManagement] Logging out user -> " + username);
            return dbAccess.setLoginStatus(username, false);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}