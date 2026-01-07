package com.example.application.ApplicationServer.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {
    private final RoomManager roomManager;

    public MatchingController(RoomManager roomManager) {
        System.out.println("MatchingController initialized with RoomManager.");
        this.roomManager = roomManager;
    }

 @PostMapping("/auto-join")
public ResponseEntity<Map<String, Object>> autoJoin(@RequestParam String playerName) {
    Room room = roomManager.findAvailableRoom();
    if (room == null) {
        room = roomManager.createRoom();
    }

    // 1. カラーリスト（CSSの色名またはカラーコード）
    List<String> colors = List.of("#ff4d4d", "#4d94ff", "#4dff88", "#ffdb4d"); // 赤, 青, 緑, 黄

    // 2. 現在の「参加済み人数」を数えて、自分の色を決める
    int playerIndex = room.getPlayers().size(); // 0人目なら0、1人目なら1...
    String myColor = colors.get(playerIndex % colors.size());
    System.out.println("Assigning color " + myColor + " to player " + playerName);

    // 3. プレイヤーを作成（名前と決まった色を渡す）
    Player me = new Player(playerName, myColor);
    room.addPlayer(me);
    System.out.println("Player " + me.getName() + " joined room " + room.getRoomId() + " with color " + myColor);

    Map<String, Object> response = new HashMap<>();
    response.put("room", room);
    response.put("me", me);
    System.out.println(me);

    return ResponseEntity.ok(response);
}

    @GetMapping("/status")
    public ResponseEntity<Room> getStatus(@RequestParam String roomId) {
        Room room = roomManager.getRoom(roomId);
        System.out.println("Fetching status for room ID: " + roomId);
        return (room != null) ? ResponseEntity.ok(room) : ResponseEntity.notFound().build();
    }
}