package com.example.application;

public class ApplicationCommunicator {
    //呼ばれたら即コネクション確立
    public ApplicationCommunicator(String systemMessage){
        this.establishConnection(systemMessage);
    }

    public void establishConnection(String systemMessage){
        //後で追加
    }
}
