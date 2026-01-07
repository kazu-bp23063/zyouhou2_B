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
        // 5マス,15マス: 単位ゲット
        squareList.put(1, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        squareList.put(3, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        squareList.put(5, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        squareList.put(9, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        squareList.put(13, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));
        squareList.put(15, new GameEvent("楽単ゲット！ 予定単位+2", 2, GameEvent.EFFECT_NONE));

        // 2マス,12マス: 一回休み
        squareList.put(2, new GameEvent("寝坊した！ 一回休み", 0, GameEvent.EFFECT_SKIP));
        squareList.put(8, new GameEvent("寝坊した！ 一回休み", 0, GameEvent.EFFECT_SKIP));
        squareList.put(16, new GameEvent("寝坊した！ 一回休み", 0, GameEvent.EFFECT_SKIP));
        squareList.put(19, new GameEvent("寝坊した！ 一回休み", 0, GameEvent.EFFECT_SKIP));
        // 7マス目,17マス目: 単位減少
        squareList.put(4, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        squareList.put(7, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        squareList.put(11, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        squareList.put(12, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        squareList.put(17, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));
        squareList.put(18, new GameEvent("必修を落とした... 予定単位-2", -2, GameEvent.EFFECT_NONE));

    }
    /**
     * 指定された位置のイベントを取得する
     * 
     * @param position 現在のマス
     * @return イベントがあればGameEvent, なければnull
     */
    public GameEvent getGameEvent(int position) {
        return squareList.get(position);
    }
}