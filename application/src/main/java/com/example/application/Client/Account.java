package com.example.application.Client;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class Account {

    @Id 
    private String username;
    
    private String password;
    
    public Account() {}

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}