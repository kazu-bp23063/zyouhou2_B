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
        // 5マス目: 単位ゲット
        squareList.put(5, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        // 10マス目: 一回休み
        squareList.put(10, new GameEvent("寝坊した！ 一回休み", 0, GameEvent.EFFECT_SKIP));
        // 15マス目: 単位減少
        squareList.put(15, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        // 18マス目: 1マス戻る
        squareList.put(18, new GameEvent("道に迷った。1マス戻る", 0, GameEvent.EFFECT_BACK));
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
