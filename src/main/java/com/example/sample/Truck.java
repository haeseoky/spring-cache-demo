package com.example.sample;

public non-sealed class Truck extends Vehicle {
    private final int maxPayload;
    
    public Truck(String registrationNumber, int maxPayload) {
        super(registrationNumber);
        this.maxPayload = maxPayload;
    }
    
    public int getMaxPayload() {
        return maxPayload;
    }
    
    @Override
    public int getWheelCount() {
        return 6; // 기본적으로 6개 바퀴를 가정합니다.
    }
}
