package com.example.application.ApplicationServer.Entity;

public class GameEvent {

    /**
     * 止まったマスの番号に応じてプレイヤーの状態を更新し、メッセージを返す
     * マス番号は cell-0 から cell-19 まで対応
     */
    public static String execute(Player player) {
        int pos = player.getCurrentPosition();
        String message = "";

        switch (pos) {
            case 3 -> {
                // 3番マス: 臨時ボーナス
                player.setExpectedUnits(player.getExpectedUnits() + 10);
                message = "💡 ゼミの資料作りを手伝った！次にもらえる単位が +10！";
            }
            case 5 -> {
                // 5番マス: 単位没収
                int current = player.getEarnedUnits();
                player.setEarnedUnits(Math.max(0, current - 15));
                message = "😱 必修科目のレポートを出し忘れた... 単位を 15 失った。";
            }
            case 8 -> {
                // 8番マス: ラッキーイベント
                player.setEarnedUnits(player.getEarnedUnits() + 20);
                message = "✨ 教授のお手伝いで特別単位を 20 ゲット！";
            }
            case 13 -> {
                // 13番マス: 強制送還
                player.setCurrentPosition(0);
                message = "💥 留年の危機！？スタート地点（0番）に強制送還！";
            }
            case 17 -> {
                // 17番マス: 期待値大幅アップ
                player.setExpectedUnits(player.getExpectedUnits() + 20);
                message = "🔥 試験対策が完璧だ！次にもらえる単位が +20！";
            }
            default -> {
                // 何も起きないマス
                message = pos + " 番目のマスに到着しました。";
            }
        }
        return message;
    }
}