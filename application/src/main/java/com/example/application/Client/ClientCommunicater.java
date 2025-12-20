package com.example.application.Client;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.Random;

@Getter
@AllArgsConstructor
public class ClientCommunicater implements Runnable{
    //タスク番号、送られてきた際は初期値の0のまま(林)
    private int taskNum=10;
    //Json化したタスクで使う情報
    private string task;
    //接続が確立したときに手に入るオブジェクト、通信パイプの役割(林)
    static Session session;
    //通信を行うためのコンテナ
    static WebSocketContainer container;
    //接続先(林)
    static String serverEndpoint ="ws://localhost:8080/clientmanager";
    static int id=1;
    //クラスのフィールドをjsonにするためのインスタンス
    static Gson gson =new Gson();
    static int sampleIncrement =0;

    //接続を確立するメソッド
    public boolean establishConnection(){
        //通信を行うためのエンジンを取得(林)
        container =ContainerProvider.getWebSocketContainer();
        try{
            //設定したエンドポイントに接続をする
            session=container.connectToServer(new WebSocketEndpointSample(),URI.create(serverEndpoint));
            return true;
        }catch(Exception e){
            return False;
        }
    }

    public void closeConnection(){
        if(session.isOpen()){
            session.close();
        }
    }

    @ClientEndPoint
    public void receiveData(String data){
        //受け取ったJsonの情報をclienttoClientManagementMessageクラスのフィールドにセット(林)
        ClienttoClientManagementMessage receiveData = gson.fromJson(data,ClienttoClientManagementMessage.class);  
    }

    public void sendData(Session session2 ,String data){
        try{
            session2.getBasicRemote().sendText(data);
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}