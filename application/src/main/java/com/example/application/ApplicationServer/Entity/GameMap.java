package com.example.application.ApplicationServer.Entity;

import java.util.HashMap;
import java.util.Map;

public class GameMap {
    private final Map<Integer, GameEvent> squareList;

    public GameMap() {
        squareList = new HashMap<>();
        initializeMap();
    }

    // マップの初期化（イベントの配置）
    private void initializeMap() {

        // ===== 取得予定単位 +5 =====（三平）
        squareList.put(1,  new GameEvent("レポートを教授に褒められる。取得予定単位数 +5", 5, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(5,  new GameEvent("発表が好評で評価アップ。取得予定単位数 +5", 5, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(9,  new GameEvent("小テスト満点！取得予定単位数 +5", 5, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(13, new GameEvent("演習が一気に進む。取得予定単位数 +5", 5, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(17, new GameEvent("勉強の習慣がつく。取得予定単位数 +5", 5, GameEvent.EFFECT_NONE)); // （三平）

        // ===== 取得予定単位 -2 =====（三平）
        squareList.put(3,  new GameEvent("提出期限を勘違い…取得予定単位数 -2", -2, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(7,  new GameEvent("寝不足で集中できない。取得予定単位数 -2", -2, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(11, new GameEvent("レポートにミス…取得予定単位数 -2", -2, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(15, new GameEvent("必修が難しい…取得予定単位数 -2", -2, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(19, new GameEvent("計画が崩れる…取得予定単位数 -2", -2, GameEvent.EFFECT_NONE)); // （三平）

        // ===== 一回休み =====（三平）
        squareList.put(2,  new GameEvent("寝坊した！次のターンは一回休み", 0, GameEvent.EFFECT_SKIP)); // （三平）
        squareList.put(10, new GameEvent("体調不良…次のターンは一回休み", 0, GameEvent.EFFECT_SKIP)); // （三平）
        squareList.put(18, new GameEvent("急用が入った…次のターンは一回休み", 0, GameEvent.EFFECT_SKIP)); // （三平）

        // ===== 2マス進む =====（三平）
        squareList.put(4,  new GameEvent("近道を発見！2マス進む", 0, GameEvent.EFFECT_BACK)); // （三平）
        squareList.put(8,  new GameEvent("友達に助けられる！2マス進む", 0, GameEvent.EFFECT_BACK)); // （三平）
        squareList.put(16, new GameEvent("集中力MAX！2マス進む", 0, GameEvent.EFFECT_BACK)); // （三平）

         // ===== 1マス戻る =====（三平）
        squareList.put(6,  new GameEvent("課題の提出場所を間違えた…1マス戻る", 0, GameEvent.EFFECT_NONE)); // （三平）
        squareList.put(14, new GameEvent("教室を間違えた…1マス戻る", 0, GameEvent.EFFECT_NONE)); // （三平）

        // ===== イベントなし =====
        // マス0, マス12 はイベントなし
    }

    /**
     * 指定された位置のイベントを取得する
     * @param position 現在のマス
     * @return イベントがあればGameEvent, なければnull
     */
    public GameEvent getGameEvent(int position) {
        return squareList.get(position);
    }
}
