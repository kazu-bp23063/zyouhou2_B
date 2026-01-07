package com.example.application.ApplicationServer.Controller;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;
import com.example.application.ClientManagementServer.Controller.DatabaseAccess;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;;
import java.util.List;

@Controller
public class ResultController {
    
    // データベース操作用クラスのインスタンス化
    private final DatabaseAccess dbAccess = new DatabaseAccess();
    // 部屋管理用マネージャーのインスタンス化
    private final RoomManager roomManager = new RoomManager();

   /**
    * リザルト画面を表示し、順位の計算とDB更新を行うエンドポイント。
    * @param roomId 対象の部屋ID
    * @param model 画面に渡すデータを保持するオブジェクト
    * @return 表示するHTMLテンプレート名
    */
   @GetMapping("/result")
   public String showResult(@RequestParam(required = false) String roomId, Model model) {
    // 部屋IDが指定されていない場合はスタート画面へ戻す
    if (roomId == null || roomId.isEmpty()) return "redirect:/start";

    // 部屋情報を取得。存在しない場合はスタート画面へ戻す
    Room room = roomManager.getRoom(roomId);
    if (room == null) return "redirect:/start";

    // 1. プレイヤーを単位数（earnedUnits）が多い順にソートする
    List<Player> players = new ArrayList<>(room.getPlayers());
    players.sort((p1, p2) -> Integer.compare(p2.getEarnedUnits(), p1.getEarnedUnits()));

    // 2. 順位の計算処理（同じ単位数のプレイヤーは同順位とする）
    List<RankedPlayer> ranking = new ArrayList<>();
    int currentRank = 1;
    for (int i = 0; i < players.size(); i++) {
        Player p = players.get(i);
        // 一つ前のプレイヤーと単位数が異なる場合、現在の並び順に基づいて順位を更新
        if (i > 0 && p.getEarnedUnits() != players.get(i - 1).getEarnedUnits()) {
            currentRank = i + 1;
        }
        // 表示用のデータ構造に格納
        ranking.add(new RankedPlayer(currentRank, p.getName(), p.getEarnedUnits()));
    }

    // 3. 戦績データの保存（多重更新防止のための排他制御）
    // 部屋オブジェクトをロックして、複数のユーザーが同時にアクセスしてもDB更新が一度だけ行われるようにする
    synchronized(room) { 
        // まだこの部屋の戦績が更新されていない場合のみDB処理を実行
        if (!room.isRankUpdated()) {
            System.out.println("\n========== [DATABASE UPDATE: " + roomId + "] ==========");
            updateRanksInDb(ranking); // データベースへの保存処理を実行
            room.setRankUpdated(true); // 更新済みフラグを立てる
            System.out.println("============================================\n");
        } else {
            // 既に他のプレイヤーのアクセスによって更新が完了している場合
            System.out.println("[Info] Room " + roomId + " is already updated. Skipping.");
        }
    }

    // 画面表示用にランキングデータをモデルに追加
    model.addAttribute("ranking", ranking);
    return "result"; // result.html を表示
}

    /**
     * 計算された順位に基づいて、データベース内の各ユーザーの戦績カウントを増やす。
     * @param ranking 順位情報を含むプレイヤーリスト
     */
    private void updateRanksInDb(List<RankedPlayer> ranking) {
        for (RankedPlayer rp : ranking) {
            // 指定された名前のユーザーの、該当順位のカウントを1増やす
            boolean success = dbAccess.incrementRankCount(rp.name(), rp.rank());
            
            // 処理結果をサーバーログに出力
            if (success) {
                System.out.printf("  [SUCCESS] Player: %-10s | Increment rank%d\n", rp.name(), rp.rank());
            } else {
                System.out.printf("  [FAILED]  Player: %-10s | User not found in database.\n", rp.name());
            }
        }
    }

    /**
     * 順位表示用の不変データ構造。
     * @param rank 順位
     * @param name プレイヤー名
     * @param earnedUnits 最終獲得単位数
     */
    public record RankedPlayer(int rank, String name, int earnedUnits) {

    }
}