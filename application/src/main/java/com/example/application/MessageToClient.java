package com.example.application;

import java.util.List;
import java.util.ArrayList;

public class MessageToClient {
    private int roomId;
    private int gameStatus;
    private int userId;
    private int expectedCredits; 
    private int earnedCredits;   
    private int doubleDiceCount;
    private int justDiceCount;
    private int currentPlace;    
    private boolean isSkipped;   
    private List<String> resultList;

    // コンストラクタ（初期化用）
    public MessageToClient() {
        this.resultList = new ArrayList<>();
    }

    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getGameStatus() {
        return gameStatus;
    }
    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
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
    public void setCurrentPlace(int currentPlace) {
        this.currentPlace = currentPlace;
    }

    public boolean getSkipped() { 
        return isSkipped;
    }
    public void setSkipped(boolean isSkipped) {
        this.isSkipped = isSkipped;
    }

    public List<String> getResultList() {
        return resultList;
    }
    public void setResultList(List<String> resultList) {
        this.resultList = resultList;
    }
}