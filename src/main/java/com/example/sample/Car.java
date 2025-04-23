package com.example.sample;

public sealed class Car extends Vehicle permits Sedan, SUV {
    private final int passengerCount;
    
    public Car(String registrationNumber, int passengerCount) {
        super(registrationNumber);
        this.passengerCount = passengerCount;
    }
    
    public int getPassengerCount() {
        return passengerCount;
    }
    
    @Override
    public int getWheelCount() {
        return 4;
    }
}
