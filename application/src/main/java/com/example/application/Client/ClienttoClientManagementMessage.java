package com.example.application.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientToClientManagementMessage {
    private String userName;
    private String password;
    private String taskName;
    private boolean isMatched;
    private int gameRecord;

}
