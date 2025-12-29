package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ClientManagementServer.DatabaseAccess; // DatabaseAccessをインポート
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ResultController {
    
    private final DatabaseAccess dbAccess = new DatabaseAccess();
    private final RoomManager roomManager = new RoomManager();

   @GetMapping("/result")
   public String showResult(@RequestParam(required = false) String roomId, Model model) {
    if (roomId == null || roomId.isEmpty()) return "redirect:/start";

    Room room = roomManager.getRoom(roomId);
    if (room == null) return "redirect:/start";

    // 1. 単位数(earnedUnits)順にソート
    List<Player> players = new ArrayList<>(room.getPlayers());
    players.sort((p1, p2) -> Integer.compare(p2.getEarnedUnits(), p1.getEarnedUnits()));

    // 2. 順位計算
    List<RankedPlayer> ranking = new ArrayList<>();
    int currentRank = 1;
    for (int i = 0; i < players.size(); i++) {
        Player p = players.get(i);
        if (i > 0 && p.getEarnedUnits() != players.get(i - 1).getEarnedUnits()) {
            currentRank = i + 1;
        }
        ranking.add(new RankedPlayer(currentRank, p.getName(), p.getEarnedUnits()));
    }

    // 3.synchronizedを使って二重更新を完全に防ぐ
    synchronized(room) { 
        if (!room.isRankUpdated()) {
            System.out.println("\n========== [DATABASE UPDATE: " + roomId + "] ==========");
            updateRanksInDb(ranking); // DatabaseAccess経由の更新
            room.setRankUpdated(true);
            System.out.println("============================================\n");
        } else {
            System.out.println("[Info] Room " + roomId + " is already updated. Skipping.");
        }
    }

    model.addAttribute("ranking", ranking);
    return "result";
}

    private void updateRanksInDb(List<RankedPlayer> ranking) {
        for (RankedPlayer rp : ranking) {
            // DatabaseAccess のメソッドを呼び出し
            boolean success = dbAccess.incrementRankCount(rp.name(), rp.rank());
            
            if (success) {
                System.out.printf("  [SUCCESS] Player: %-10s | Increment rank%d\n", rp.name(), rp.rank());
            } else {
                System.out.printf("  [FAILED]  Player: %-10s | User not found in database.\n", rp.name());
            }
        }
    }

    // 順位表示用のデータ構造
    public record RankedPlayer(int rank, String name, int earnedUnits) {

    }
}