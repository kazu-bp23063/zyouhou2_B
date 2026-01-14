package com.example.application;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ApplicationServer.Entity.Player;

class RoomTest {

    @Test
    void testRoomInitialization() {
        Room room = new Room();
        // IDが生成されているか
        assertNotNull(room.getRoomId());
        // 初期状態ではプレイヤーは0人か確認
        assertTrue(room.getPlayers().isEmpty());
    }

    @Test
    void testAddPlayer() {
        Room room = new Room();
        Player p1 = new Player("Alice", "#ff4d4d");
        
        room.addPlayer(p1);
        
        assertEquals(1, room.getPlayers().size());
        assertEquals("Alice", room.getPlayers().get(0).getName());
    }

    @Test
    void testMaxPlayerLimit() {
        Room room = new Room();
        
        // 4人追加する
        room.addPlayer(new Player("P1", "red"));
        room.addPlayer(new Player("P2", "blue"));
        room.addPlayer(new Player("P3", "green"));
        room.addPlayer(new Player("P4", "yellow"));
        
        assertEquals(4, room.getPlayers().size());

        // 5人目を追加しようとする
        room.addPlayer(new Player("P5", "black"));

        // それでも4人のままであるべき
        assertEquals(4, room.getPlayers().size());
    }
}