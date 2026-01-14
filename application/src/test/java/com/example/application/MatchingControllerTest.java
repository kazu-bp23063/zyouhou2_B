package com.example.application;

import com.example.application.ApplicationServer.Controller.RoomManager;
import com.example.application.ApplicationServer.Controller.MatchingController;
import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchingControllerTest {

    // RoomManagerのモックを作成
    @Mock
    private RoomManager roomManager;

    // モックを注入したテスト対象のControllerを作成
    @InjectMocks
    private MatchingController matchingController;

    @BeforeEach
    void setUp() {
        // Mockitoの初期化
        MockitoAnnotations.openMocks(this);
    }

    /* ケース1: 空き部屋がない場合
      新しい部屋が作成され、そこにプレイヤー(赤色)が追加されることを確認
     */
    @Test
    void testAutoJoin_CreatesNewRoom_WhenNoAvailableRoom() {
        // 準備: findAvailableRoomがnullを返す設定
        when(roomManager.findAvailableRoom()).thenReturn(null);
        
        // createRoomが呼ばれたら新しい部屋を返す設定
        Room newRoom = new Room();
        when(roomManager.createRoom()).thenReturn(newRoom);

        // 実行
        ResponseEntity<Map<String, Object>> response = matchingController.autoJoin("TestUser");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(roomManager, times(1)).createRoom(); // createRoomが1回呼ばれたか

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("me"));
        assertTrue(body.containsKey("room"));
        
        Player me = (Player) body.get("me");
        assertEquals("TestUser", me.getName());
        assertEquals("#ff4d4d", me.getColor()); // 1人目なので赤色（colorsリストの0番目）
        
        // 部屋にプレイヤーが追加されているか
        assertEquals(1, ((Room)body.get("room")).getPlayers().size());
    }

    /*
      ケース2: 空き部屋がある場合
      -> 既存の部屋にプレイヤー(青色など)が追加され、createRoomは呼ばれないことを確認
     */
    @Test
    void testAutoJoin_JoinsExistingRoom() {
        // 準備: すでに1人(赤)がいる部屋を用意
        Room existingRoom = new Room();
        existingRoom.addPlayer(new Player("ExistingUser", "#ff4d4d")); 
        
        // findAvailableRoomがその部屋を返す設定
        when(roomManager.findAvailableRoom()).thenReturn(existingRoom);

        // 実行
        ResponseEntity<Map<String, Object>> response = matchingController.autoJoin("Joiner");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(roomManager, never()).createRoom(); // createRoomは呼ばれてはいけない

        Player me = (Player) response.getBody().get("me");
        assertEquals("Joiner", me.getName());
        assertEquals("#4d94ff", me.getColor()); // 2人目なので青色（colorsリストの1番目）
    }

    /*
      ケース3: ステータス取得（部屋が存在する場合）
     */
    @Test
    void testGetStatus_Found() {
        Room room = new Room();
        String roomId = room.getRoomId();
        when(roomManager.getRoom(roomId)).thenReturn(room);

        ResponseEntity<Room> response = matchingController.getStatus(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(room, response.getBody());
    }

    /*
      ケース4: ステータス取得（部屋が存在しない場合）
     */
    @Test
    void testGetStatus_NotFound() {
        String invalidId = "UNKNOWN";
        when(roomManager.getRoom(invalidId)).thenReturn(null);

        ResponseEntity<Room> response = matchingController.getStatus(invalidId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}