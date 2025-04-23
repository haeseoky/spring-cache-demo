package com.example.sample;

public final class Sedan extends Car {
    private final boolean hasTrunk;
    
    public Sedan(String registrationNumber, int passengerCount, boolean hasTrunk) {
        super(registrationNumber, passengerCount);
        this.hasTrunk = hasTrunk;
    }
    
    public boolean hasTrunk() {
        return hasTrunk;
    }
}
