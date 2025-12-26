package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import com.example.application.ApplicationServer.Entity.Room;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RoomManager {
    // 【重要】staticを追加して、どのインスタンスからも同じデータを見れるようにする
    private static final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

    public Room getRoom(String roomId) {
        return activeRooms.get(roomId);
    }

    public Room createRoom() {
        Room newRoom = new Room();
        activeRooms.put(newRoom.getRoomId(), newRoom);
        return newRoom;
    }

    public Room findAvailableRoom() {
        return activeRooms.values().stream()
                .filter(room -> room.getPlayers().size() < 4)
                .findFirst()
                .orElse(null);
    }
}