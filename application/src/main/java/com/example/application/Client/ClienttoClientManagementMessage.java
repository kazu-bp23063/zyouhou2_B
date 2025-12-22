package com.example.application.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClienttoClientManagementMessage {
    private String userName;
    private int passWord;
    private String task;
    private boolean isMatched;
    private int gameRecord;

}
