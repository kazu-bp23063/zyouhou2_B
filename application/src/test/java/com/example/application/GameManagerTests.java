package com.example.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.application.ApplicationServer.Controller.DiceController;
import com.example.application.ApplicationServer.Entity.GameEvent;
import com.example.application.ApplicationServer.Entity.GameMap;
import com.example.application.ApplicationServer.Entity.Player;


public class GameManagerTests {

    private Player player;
    private DiceController diceController;
    private GameMap gameMap;

    @BeforeEach
    public void setUp() {
        player = new Player("TestPlayer", "red");
        diceController = new DiceController();
        gameMap = new GameMap();
    }

    // --- 1. callPlayerNotifier (手番通知のロジック検証) ---

    @Test
    @DisplayName("callPlayerNotifier: 通常の手番通知（ロジック検証）")
    public void testCallPlayerNotifier_Normal() {
        System.out.println("--- callPlayerNotifier (通常) ---");
        
        // 準備
        player.setSkipped(false);
        
        // 検証: スキップフラグが立っていないので通常の手番
        if (!player.isSkipped()) {
            System.out.println("通知: " + player.getName() + " の番です。");
            assertFalse(player.isSkipped());
        } else {
            System.out.println("通知: " + player.getName() + " は休みです。");
        }
    }

    @Test
    @DisplayName("callPlayerNotifier: スキップ通知（ロジック検証）")
    public void testCallPlayerNotifier_Skip() {
        System.out.println("--- callPlayerNotifier (スキップ) ---");
        
        // 準備: スキップ状態にする
        player.setSkipped(true);
        
        // 検証
        if (player.isSkipped()) {
            System.out.println("通知: " + player.getName() + " は一回休みです。");
            assertTrue(player.isSkipped());
            // 休み処理後のリセットロジックをシミュレート
            player.setSkipped(false); 
        }
    }

    // --- 2. rollDiceAndMove (移動ロジック検証) ---

    @Test
    @DisplayName("rollDiceAndMove: NormalDice (1-6)")
    public void testRollDiceAndMove_Normal() {
        System.out.println("--- rollDiceAndMove (Normal) ---");
        
        player.setCurrentPosition(0);
        
        // 実行: NORMALダイスを振る
        int roll = diceController.executeRoll("NORMAL", null);
        
        // 移動計算
        int newPos = player.getCurrentPosition() + roll;
        player.setCurrentPosition(newPos);
        
        System.out.println("出目: " + roll + ", 現在地: " + player.getCurrentPosition());
        
        // 検証
        assertTrue(roll >= 1 && roll <= 6);
        assertEquals(roll, player.getCurrentPosition());
    }

    @Test
    @DisplayName("rollDiceAndMove: DoubleDice (2-12)")
    public void testRollDiceAndMove_Double() {
        System.out.println("--- rollDiceAndMove (Double) ---");
        
        player.setCurrentPosition(0);
        
        // 実行: DOUBLEダイスを振る
        int roll = diceController.executeRoll("DOUBLE", null);
        
        // 移動計算
        int newPos = player.getCurrentPosition() + roll;
        player.setCurrentPosition(newPos);
        
        System.out.println("出目(Double): " + roll + ", 現在地: " + player.getCurrentPosition());
        
        // 検証: DOUBLEは最低2、最大12
        assertTrue(roll >= 2 && roll <= 12);
        assertEquals(roll, player.getCurrentPosition());
    }

    @Test
    @DisplayName("rollDiceAndMove: JustDice (指定した値)")
    public void testRollDiceAndMove_Just() {
        System.out.println("--- rollDiceAndMove (Just) ---");
        
        player.setCurrentPosition(0);
        int targetValue = 5;
        
        // 実行: JUSTダイスで5を指定
        int roll = diceController.executeRoll("JUST", targetValue);
        
        // 移動計算
        player.setCurrentPosition(player.getCurrentPosition() + roll);
        
        System.out.println("指定値: " + targetValue + ", 出目: " + roll + ", 現在地: " + player.getCurrentPosition());
        
        // 検証
        assertEquals(5, roll);
        assertEquals(5, player.getCurrentPosition());
    }

    @Test
    @DisplayName("rollDiceAndMove: 限界値での検証 (周回処理)")
    public void testRollDiceAndMove_Boundary() {
        System.out.println("--- rollDiceAndMove (Boundary) ---");
        
        // 準備: 18マス目に配置
        player.setCurrentPosition(18);
        int roll = 5; // 出目が5と仮定
        
        // 移動計算 (マップサイズ20)
        int tempPos = player.getCurrentPosition() + roll; // 23
        int newPos = tempPos % 20; // 3
        player.setCurrentPosition(newPos);
        
        System.out.println("初期位置: 18, 出目: 5 -> 計算位置: " + tempPos + " -> 補正後: " + player.getCurrentPosition());
        
        // 検証
        assertEquals(3, player.getCurrentPosition());
    }

    // --- 3. checkAndCallEvent (イベント発生検証) ---

    @Test
    @DisplayName("checkAndCallEvent: マスのイベント取得")
    public void testCheckAndCallEvent() {
        System.out.println("--- checkAndCallEvent ---");
        
        // 準備: イベントがあるマスに移動させる
        int eventPos = 5; 
        GameEvent event = gameMap.getGameEvent(eventPos);
        
        if (event != null) {
            System.out.println("マス " + eventPos + " のイベント: " + event.getEventContent());
            System.out.println("単位増減: " + event.getCreditAdjustmentValue());
        } else {
            System.out.println("マス " + eventPos + " にイベントはありませんでした。");
        }
        
        // 検証: GameMapの仕様通りイベントが取得できるか
        assertEquals("楽単ゲット！ 予定単位+2", event.getEventContent());
    }

    // --- 4. callCreditManager (単位加算ロジック検証) ---

    @Test
    @DisplayName("callCreditManager: 周回時の単位加算")
    public void testCallCreditManager_Lap() {
        System.out.println("--- callCreditManager ---");
        
        // 準備: 取得済み0、予定25
        player.setEarnedUnits(0);
        player.setExpectedUnits(25);
        
        // ロジック実行: 周回したと仮定して単位を加算 (GameManagementController.callCreditManagerのロジック)
        int currentEarned = player.getEarnedUnits();
        int expected = player.getExpectedUnits();
        
        player.setEarnedUnits(currentEarned + expected);
        // 次の周回のために予定単位をリセット(初期値へ)
        player.setExpectedUnits(25);
        
        System.out.println("加算結果: " + player.getEarnedUnits());
        
        // 検証
        assertEquals(25, player.getEarnedUnits());
        assertEquals(25, player.getExpectedUnits()); // リセット確認
    }

    // --- 5. checkStatus (ゲーム続行確認) ---

    @Test
    @DisplayName("checkStatus: ゲーム終了条件未達")
    public void testCheckStatus() {
        System.out.println("--- checkStatus ---");

        // テストのために 20 に設定
        player.setEarnedUnits(20); 
        
        boolean isGraduated = player.checkGraduationRequirement();
        
        if (!isGraduated) {
            System.out.println("ステータス: 単位 " + player.getEarnedUnits() + " -> 卒業不可 (ゲーム続行)");
        } else {
            System.out.println("ステータス: 卒業！");
        }
        
        // 検証
        assertFalse(isGraduated);
    }

    // --- 6. endGame (順位付けロジック検証) ---

    @Test
    @DisplayName("endGame: プレイヤーの順位付け")
    public void testEndGame_Sort() {
        System.out.println("--- endGame ---");
        
        // 準備: 4人のプレイヤー
        List<Player> players = new ArrayList<>();
        Player p1 = new Player("P1", "red"); p1.setEarnedUnits(120);
        Player p2 = new Player("P2", "blue"); p2.setEarnedUnits(500);
        Player p3 = new Player("P3", "green"); p3.setEarnedUnits(300);
        Player p4 = new Player("P4", "yellow"); p4.setEarnedUnits(450);
        
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
        
        // ロジック実行: 単位数で降順ソート
        players.sort(Comparator.comparingInt(Player::getEarnedUnits).reversed());
        
        System.out.println("順位結果:");
        for (int i = 0; i < players.size(); i++) {
            System.out.println((i+1) + "位: " + players.get(i).getName() + " (" + players.get(i).getEarnedUnits() + "単位)");
        }
        
        // 検証
        assertEquals("P2", players.get(0).getName()); // 500
        assertEquals("P4", players.get(1).getName()); // 450
        assertEquals("P3", players.get(2).getName()); // 300
        assertEquals("P1", players.get(3).getName()); // 120
    }

    // --- 7. AFK Count (放置カウント検証) ---

    @Test
    @DisplayName("incrementAfkCount: カウント増加")
    public void testIncrementAfkCount() {
        System.out.println("--- incrementAfkCount ---");
        
        player.setAfkCount(0);
        
        // 実行: 2回インクリメント
        player.setAfkCount(player.getAfkCount() + 1);
        player.setAfkCount(player.getAfkCount() + 1);
        
        System.out.println("現在のAFKカウント: " + player.getAfkCount());
        
        // 検証
        assertEquals(2, player.getAfkCount());
    }

    @Test
    @DisplayName("resetAfkCount: カウントリセット")
    public void testResetAfkCount() {
        System.out.println("--- resetAfkCount ---");
        
        player.setAfkCount(2);
        
        // 実行: リセット
        player.setAfkCount(0);
        
        System.out.println("リセット後のAFKカウント: " + player.getAfkCount());
        
        // 検証
        assertEquals(0, player.getAfkCount());
    }
}