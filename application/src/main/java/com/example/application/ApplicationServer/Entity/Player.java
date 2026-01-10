package com.example.application.ApplicationServer.Entity;

import java.util.UUID;

import lombok.Data;

@Data
public class Player {
    private String id;
    private String name;
    private String color;
    private int currentPosition;
    
    // 元の変数名(Units)を維持します
    private int earnedUnits;
    private int expectedUnits;

    // 追加: 一回休みフラグ
    private boolean isSkipped;
    // 追加: 放置カウント
    private int afkCount;

    // ★追加: アイテム使用済みフラグ
    private boolean usedDouble;
    private boolean usedJust;

    public Player(String name, String color) {
        this.id = UUID.randomUUID().toString(); 
        this.name = name;
        this.color = color;
        this.currentPosition = 0;
        this.earnedUnits = 0;
        this.expectedUnits = 25; // 初期予定単位
        this.isSkipped = false;
        this.afkCount = 0;

        this.usedDouble = false;
        this.usedJust = false;
    }

    // 卒業要件チェック (124単位以上)
    public boolean checkGraduationRequirement() {
        return this.earnedUnits >= 124;
    }
}