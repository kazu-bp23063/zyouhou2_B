package com.example.application.Client.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientToClientManagementMessage {
    private String taskName;
    private String userId;
    private String userName;
    private String password;
}
