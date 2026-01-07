package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import lombok.Data;

@Data
@Service
public class CreditManager {
    private int earnedUnits = 0; 
    private int expectedUnits = 25;
    private final int GRADUATION_UNITS = 25; 

    public void graduateUnit() {
        System.out.println("Graduating units. Current earned units: " + this.earnedUnits + ", expected units to graduate: " + this.expectedUnits);
        this.earnedUnits += this.expectedUnits;
    }

    public boolean isGraduated() {
        System.out.println("Current earned units: " + this.earnedUnits);
        return this.earnedUnits >= GRADUATION_UNITS;
    }

    public void reset() {
        this.earnedUnits = 0;
        this.expectedUnits = 25;
        System.out.println("Credits have been reset.");
    }
}
