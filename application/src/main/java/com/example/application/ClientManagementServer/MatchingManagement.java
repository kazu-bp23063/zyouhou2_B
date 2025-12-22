package com.example.application.ClientManagementServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.application.ClientManagementServer.Message.ApplicationMessage;
import com.example.application.ClientManagementServer.Message.AppRoomCreatedMessage;
import com.example.application.ClientManagementServer.Message.ApplicationToClientManagementMessage;
import com.example.application.ClientManagementServer.Message.ApplicationMessage.PlayerInfo;
import com.google.gson.Gson;

import jakarta.websocket.Session;
import lombok.AllArgsConstructor;

public class MatchingManagement {
    /** プレイヤー待ち行列 */
    private Deque<PlayerEntry> matchingWaitList = new ArrayDeque<>();

    /**
     * matchId -> その matchId に紐づくプレイヤーグループ
     * ApplicationServer にルーム作成依頼を出してから、roomId が返るまでの「待ち状態」を保持する
     */
    private Map<String, List<PlayerEntry>> roomCreationWaitMap = new HashMap<>();

    private ApplicationServerClient appClient = new ApplicationServerClient(this);

    Gson gson = new Gson();

    /* 待ち行列に追加 */
    public void addUserToWaitList(Session session, String userName, String userId) {
        matchingWaitList.addLast(new PlayerEntry(userName, userId, session));
        System.out.println("[Matching] Player " + userName + " joined the matchingWaitList. matchingWaitList size: "
                + matchingWaitList.size());

        /* 規定人数以上になったらグループ化して ApplicationServer に通知 */
        if (matchingWaitList.size() >= 4) {
            // matchedPlayers は「今回マッチしたプレイヤー一覧」
            List<PlayerEntry> matchedPlayers = new ArrayList<>(4);

            /* 先頭から必要人数分取り出す */
            for (int i = 0; i < 4; i++)
                matchedPlayers.add(matchingWaitList.removeFirst());

            sendPlayerIds(matchedPlayers);
        }
    }

    /**
     * ApplicationServer に「ルーム作成依頼」を送る
     * 送信前に roomCreationWaitMap に (matchId -> group) を登録して、応答時に対応付けられるようにする
     */
    private void sendPlayerIds(List<PlayerEntry> group) {
        /* ApplicationServer と照合するためのマッチID */
        String matchId = "match-" + UUID.randomUUID();

        /* ルーム作成待ちの状態を管理するために、マッチIDとグループをマップに保存する */
        roomCreationWaitMap.put(matchId, group);

        List<PlayerInfo> players = group.stream()
                .map(p -> new PlayerInfo(p.userId, p.userName))
                .toList();

        /* ルーム作成要求メッセージをJSON化して送信 */
        ApplicationMessage msg = new ApplicationMessage("CREATE_ROOM", matchId, players);
        String json = gson.toJson(msg);

        appClient.sendMessage(json);
    }

    /**
     * ApplicationServer から roomId が返ってきたタイミングで呼ばれる
     * matchId から待機中グループを引き当て、各クライアントへ「マッチ成立」を通知する
     */
    public void onRoomCreated(String json) {
        AppRoomCreatedMessage msg = gson.fromJson(json, AppRoomCreatedMessage.class);

        // 待機中グループを取り出して削除
        List<PlayerEntry> group = roomCreationWaitMap.remove(msg.getMatchId());

        ApplicationToClientManagementMessage event = new ApplicationToClientManagementMessage("MATCH_FOUND",
                msg.getMatchId(),
                msg.getRoomId());
        String eventJson = gson.toJson(event);

        /* 各プレイヤーへ通知 */
        for (PlayerEntry player : group) {
            System.out.println("[Matching] Notifying player " + player.userName + " of match " + msg.getMatchId());
            player.session.getAsyncRemote().sendText(eventJson);
        }
    }

    /* 待ち行列に入っているプレイヤー1人分の情報 */
    @AllArgsConstructor
    private static class PlayerEntry {
        private String userName;
        private String userId;
        private Session session;
    }
}
