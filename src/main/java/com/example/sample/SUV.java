package com.example.sample;

public final class SUV extends Car {
    private final boolean hasAllWheelDrive;
    
    public SUV(String registrationNumber, int passengerCount, boolean hasAllWheelDrive) {
        super(registrationNumber, passengerCount);
        this.hasAllWheelDrive = hasAllWheelDrive;
    }
    
    public boolean hasAllWheelDrive() {
        return hasAllWheelDrive;
    }
}
