package com.example.application.ApplicationServer.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameEvent {
    // イベントの内容（文章で表示するためStringに変更）
    private String eventContent;
    // 単位の増減値
    private int creditAdjustmentValue;
    // イベントの効果 (0:なし, 1:一回休み, 2:一マス戻る)
    private int eventEffect;

    // 定数定義
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_SKIP = 1;
    public static final int EFFECT_BACK = 2;
}