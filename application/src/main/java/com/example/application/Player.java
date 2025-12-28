package com.example.application;

public class Player {
    private int userId;
    private int expectedCredits; 
    private int earnedCredits;   
    private int doubleDiceCount; 
    private int justDiceCount;   
    private int currentPlace;    
    private boolean isSkipped;
    
    public final int requiredCredits = 124; // 卒業に必要な単位数
    public final int mapSize = 20; // マップ全体のマス数

    // コンストラクタ（ユーザーIDを指定して作成）
    public Player(int userId) {
        this.userId = userId;
        initUser(); // 作成時に初期化
    }

    // ユーザーの状態を初期化するメソッド？
    public void initUser() {
        this.earnedCredits = 0;
        this.doubleDiceCount = 0;
        this.justDiceCount = 0;
        this.currentPlace = 0; 
        this.isSkipped = false;
    }



    public boolean checkGraduationRequirement(int nextPlace) {
        if (this.earnedCredits >= this.requiredCredits) {//単位が足り、
            if (nextPlace >= this.mapSize) { //スタートマスに戻った場合
                return true;   
            }
        }
        return false;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getExpectedCredits() {
        return expectedCredits;
    }
    public void setExpectedCredits(int expectedCredits) {
        this.expectedCredits = expectedCredits;
    }

    public int getEarnedCredits() {
        return earnedCredits;
    }
    public void setEarnedCredits(int earnedCredits) {
        this.earnedCredits = earnedCredits;
    }

    public int getDoubleDiceCount() {
        return doubleDiceCount;
    }
    public void setDoubleDiceCount(int doubleDiceCount) {
        this.doubleDiceCount = doubleDiceCount;
    }

    public int getJustDiceCount() {
        return justDiceCount;
    }
    public void setJustDiceCount(int justDiceCount) {
        this.justDiceCount = justDiceCount;
    }

    public int getCurrentPlace() {
        return currentPlace;
    }
    public void setCurrentPlace(int rolledNumber) {
        this.checkGraduationRequirement(this.currentPlace + rolledNumber);
        this.currentPlace += rolledNumber;
        this.currentPlace %= mapSize; // マップの範囲内に収める
    }

    public boolean getSkipped() {
        return isSkipped;
    }
    public void setSkipped(boolean isSkipped) {
        this.isSkipped = isSkipped;
    }
}