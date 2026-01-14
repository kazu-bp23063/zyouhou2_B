package com.example.application;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.application.ApplicationServer.Controller.RoomManager;
import com.example.application.ApplicationServer.Entity.Room;

class RoomManagerTest {

    private RoomManager roomManager;

    @BeforeEach
    void setUp() {
        // テストの前に毎回インスタンスを作る
        roomManager = new RoomManager();
    }

    @Test
    void testCreateRoom() {
        Room createdRoom = roomManager.createRoom();

        // チェック
        assertNotNull(createdRoom, "部屋が生成されていること");
        assertNotNull(createdRoom.getRoomId(), "部屋IDが設定されていること");
        
        // ログ確認用
        System.out.println("テストで作成された部屋ID: " + createdRoom.getRoomId());
    }

    @Test
    void testGetRoom() {
        // 1. まず部屋を作る
        Room newRoom = roomManager.createRoom();
        String roomId = newRoom.getRoomId();

        // 2. IDを使って部屋を取り出す
        Room foundRoom = roomManager.getRoom(roomId);

        // 3. 同じ部屋かチェック
        assertEquals(newRoom, foundRoom, "作成した部屋と同じものが取得できること");
    }

    @Test
    void testFindAvailableRoom() {
        // 1. 部屋を作る
        roomManager.createRoom();

        // 2. 空き部屋を探す
        Room availableRoom = roomManager.findAvailableRoom();

        // 3. 部屋が見つかるかチェック
        assertNotNull(availableRoom, "空き部屋が見つかること");
    }
}