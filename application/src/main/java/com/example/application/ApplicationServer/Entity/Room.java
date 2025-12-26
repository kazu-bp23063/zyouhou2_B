package com.example.application.ApplicationServer.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class Room {
    private String roomId;
    private List<Player> players = new ArrayList<>();
    private int turnIndex = 0;
    private final int MAX_PLAYERS = 4;
    private boolean rankUpdated = false;

    public Room() {
        this.roomId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    public void addPlayer(Player player) {
        if (players.size() < MAX_PLAYERS) {
            players.add(player);
        }
    }
}