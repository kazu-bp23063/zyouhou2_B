package com.example.application.ApplicationServer.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationToClientManagementMessage {
    public String taskName;
    public String matchId;
    public String roomId;
}