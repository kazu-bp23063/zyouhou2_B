package com.example.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.websocket.Session;

public class Room {
    private List<Session> sessionList = new ArrayList<>();
    
    // この部屋にいるプレイヤーのリスト
    private List<Player> playerList;

    //ゲームマネージャー
    private GameManager gameManager;

    // プレイヤーリストやゲームマネージャーをインスタンス化するコンストラクタ
    public Room() {
        this.playerList = new ArrayList<>();
        this.gameManager = new GameManager();
    }

    public void initPlayerList(List<Integer> userIds) {
        this.playerList.clear();
        for (Integer id : userIds) {
            // Playerクラスのコンストラクタを呼んでリストに追加
            Player p = new Player(id);
            this.playerList.add(p);
        }
        
        // メンバーが揃ったら順番をシャッフルする
        shuffleList();
        // メンバーと順番が決まったら、GameManagerにプレイヤーリストを渡す
        this.gameManager.setPlayers(this.playerList);
    }

    public void shuffleList() {
        Collections.shuffle(this.playerList);//なんと、簡単にシャッフルできるメソッドがある
    }

    public void executeOperation(ClientToApplication message, Session session) {
        
        if (message.getPushDiceRoll() != null) {
            String diceType = message.getPushDiceRoll(); // "Normal", "Double" などが入っている想定
            System.out.println("ダイスロール要求: " + diceType);
            
            // GameManagerに依頼
            this.gameManager.rollDiceAndMove(diceType);
        }
        
        // ステータス確認
        else if (message.getPushStatusButton() != null) {
            System.out.println("ステータス確認要求");
            //messageにPlayerIDが含まれている想定
            this.gameManager.checkStatus(message.getPlayerId());
        }
        
        /*
            selectとdecideとRollがあるってことは、
            決定ボタンを選んで選択したいんだろうけど、
            それっていちいちサーバーに送る必要あるか？
            賽を振るときにだけサイコロの種類をサーバーに送ってほしいな。。。
        */
        else if (message.getPushDiceDecide() != null) {
             System.out.println("処理はスキップされました。");
        }
        
        else {
            System.out.println("不明なコマンドです。");
        }
    }

    //GameManager から直接呼ばれる
    //これにより、授業中に抱えていた不安は解消？
    public void notifyToAll(MessageToClient msg) {
       for (Session session : this.sessionList) {
            // 自分で変換せず、コミュニケータにオブジェクトをそのまま渡す
            ApplicationCommunicator.sendData(session, msg);
       }
    }
}