package com.example.application.Client.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data

@Entity 
public class Account {

    @Id 
    private String username;
    private String password;
    private String id; 
    private int rank1;
    private int rank2;
    private int rank3;
    private int rank4;
}