package com.example.sample;

public final class Motorcycle extends Vehicle {
    private final boolean hasSidecar;
    
    public Motorcycle(String registrationNumber, boolean hasSidecar) {
        super(registrationNumber);
        this.hasSidecar = hasSidecar;
    }
    
    public boolean hasSidecar() {
        return hasSidecar;
    }
    
    @Override
    public int getWheelCount() {
        return hasSidecar ? 3 : 2;
    }
}
