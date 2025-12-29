package com.example.application.ApplicationServer.Message;

import lombok.Data;
import java.util.List;

@Data
public class ApplicationMessage {
    private String taskName; // "CREATE_ROOM" など
    private String matchId;
    private List<PlayerInfo> players;

    @Data
    public static class PlayerInfo {
        private String userId;
        private String userName;
    }
}