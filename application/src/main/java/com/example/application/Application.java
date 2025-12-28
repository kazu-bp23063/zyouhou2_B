package com.example.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.websocket.Session;

public class RoomManager {
    
	//ルームとルームIDの対応表
    private static Map<Integer, Room> roomList = new HashMap<>();
    
    // 部屋IDを割り振るためのカウンター
    private static int nextRoomId = 1;

	//部屋を作ってMapに登録する
    public void createRoom(List<Integer> playerIds) {
        int roomId = nextRoomId++;
        Room newRoom = new Room();

        newRoom.initPlayerList(playerIds);
        roomList.put(roomId, newRoom);
    }

	//部屋IDから部屋を探す
    public Room searchRoom(int roomId) {
        return roomList.get(roomId);
    }

	//クライアントからのメッセージを適切な部屋に転送する
    public void transferData(ClientToApplication message) {
        // メッセージから部屋IDを取得
		// ここはどうやるか決める必要あり
		// できればメッセージにはルームのIDやプレイヤーIDも含めてほしい
        int targetRoomId = message.getRoomId();
        
        //部屋を探し、処理を丸投げする
        Room targetRoom = searchRoom(targetRoomId);
        
        if (targetRoom != null) {
            targetRoom.executeOperation(message);
        } else {
            System.out.println("[RoomManager] 指定された部屋が見つかりません ID: " + targetRoomId);
        }
    }
}