package com.example.application.ApplicationServer.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {
    private final RoomManager roomManager;

    public MatchingController(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    // (auto-joinメソッドは省略可、またはそのまま)
    @PostMapping("/auto-join")
    public ResponseEntity<Map<String, Object>> autoJoin(@RequestParam String playerName) {
        // ... (省略: 必要なら元のコードを使用)
        return ResponseEntity.ok(Map.of("message", "not implemented in this snippet"));
    }

    @GetMapping("/status")
    public ResponseEntity<Room> getStatus(@RequestParam String roomId) {
        Room room = roomManager.getRoom(roomId);
        return (room != null) ? ResponseEntity.ok(room) : ResponseEntity.notFound().build();
    }

    @PostMapping("/register-room")
    public ResponseEntity<String> registerRoomFromManagement(@RequestBody Map<String, Object> data) {
        String roomId = (String) data.get("roomId");
        System.out.println("[MatchingController] Registering room: " + roomId);

        Room room = new Room();
        room.setRoomId(roomId);
        Number turnIdx = (Number) data.getOrDefault("turnIndex", 0);
        room.setTurnIndex(turnIdx.intValue());

        List<Map<String, Object>> playersData = (List<Map<String, Object>>) data.get("players");
        
        if (playersData == null || playersData.isEmpty()) {
            System.out.println("[MatchingController] 警告: 受信したプレイヤーリストが空です！ RoomID: " + roomId);
        } else {
            System.out.println("[MatchingController] 受信したプレイヤー数: " + playersData.size());
            for (Map<String, Object> pData : playersData) {
                String name = (String) pData.get("name");
                String color = (String) pData.get("color");
                String id = (String) pData.get("id");

                Player player = new Player(name, color);
                if (id != null) {
                    player.setId(id);
                }

                Number pos = (Number) pData.getOrDefault("currentPosition", 0);
                Number earned = (Number) pData.getOrDefault("earnedUnits", 0);
                Number expected = (Number) pData.getOrDefault("expectedUnits", 25);

                player.setCurrentPosition(pos.intValue());
                player.setEarnedUnits(earned.intValue());
                player.setExpectedUnits(expected.intValue());

                room.addPlayer(player);
                System.out.println("[MatchingController] Added: " + name + " (ID: " + id + ")");
            }
        }

        roomManager.addRoom(room);
        System.out.println("[MatchingController] 部屋登録完了。現在のプレイヤー数: " + room.getPlayers().size());

        return ResponseEntity.ok("Success");
    }
}