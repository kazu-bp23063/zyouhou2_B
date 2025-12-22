package com.example.application.ClientManagementServer.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AppRoomCreatedMessage {
    private String taskName;
    private String matchId;
    private String roomId;
}