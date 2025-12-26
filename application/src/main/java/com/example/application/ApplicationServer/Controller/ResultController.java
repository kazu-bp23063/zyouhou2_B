package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.Client.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ResultController {
    
    @Autowired
    private AccountRepository accountRepository;

    private final RoomManager roomManager = new RoomManager();

    @GetMapping("/result")
    public String showResult(@RequestParam(required = false) String roomId, Model model) {
        if (roomId == null || roomId.isEmpty()) return "redirect:/start";

        Room room = roomManager.getRoom(roomId);
        if (room == null) return "redirect:/start";

        // 1. 単位数(earnedUnits)順にソート
        List<Player> players = new ArrayList<>(room.getPlayers());
        players.sort((p1, p2) -> Integer.compare(p2.getEarnedUnits(), p1.getEarnedUnits()));

        // 2. 順位計算とコンソール用データの作成
        List<RankedPlayer> ranking = new ArrayList<>();
        int currentRank = 1;
        
        System.out.println("\n========== [GAME RESULT: " + roomId + "] ==========");
        
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            // 前のプレイヤーと単位数が異なる場合、順位を更新する
            if (i > 0 && p.getEarnedUnits() != players.get(i - 1).getEarnedUnits()) {
                currentRank = i + 1;
            }
            ranking.add(new RankedPlayer(currentRank, p.getName(), p.getEarnedUnits()));
            
            // 順位結果をコンソールに表示
            System.out.printf("[%d位] %s : %d 単位\n", currentRank, p.getName(), p.getEarnedUnits());
        }

        // 3. データベース更新と実行結果の出力
        if (!room.isRankUpdated()) {
            System.out.println("------------------------------------------");
            System.out.println("[Database Update Log]");
            updateAndPrintDbStatus(ranking);
            room.setRankUpdated(true);
        } else {
            System.out.println("------------------------------------------");
            System.out.println("[Info] Database already updated for this room.");
        }
        
        System.out.println("==========================================\n");

        model.addAttribute("ranking", ranking);
        return "result";
    }

    private void updateAndPrintDbStatus(List<RankedPlayer> ranking) {
        for (RankedPlayer rp : ranking) {
            // ユーザーID（名前）で検索
            accountRepository.findById(rp.name()).ifPresentOrElse(acc -> {
                int newVal = 0;
                switch (rp.rank()) {
                    case 1 -> { acc.setRank1(acc.getRank1() + 1); newVal = acc.getRank1(); }
                    case 2 -> { acc.setRank2(acc.getRank2() + 1); newVal = acc.getRank2(); }
                    case 3 -> { acc.setRank3(acc.getRank3() + 1); newVal = acc.getRank3(); }
                    case 4 -> { acc.setRank4(acc.getRank4() + 1); newVal = acc.getRank4(); }
                }
                accountRepository.save(acc);
                // 更新成功をSystem.outに出力
                System.out.printf("  [SUCCESS] Player: %-10s | Rank%d Count -> %d\n", rp.name(), rp.rank(), newVal);
            }, () -> {
                // ユーザーが見つからなかった場合
                System.out.printf("  [ERROR]   Player: %-10s | Not found in Database.\n", rp.name());
            });
        }
    }

    public record RankedPlayer(int rank, String name, int earnedUnits) {}
}