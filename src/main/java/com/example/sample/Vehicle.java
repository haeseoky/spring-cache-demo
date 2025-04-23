package com.example.sample;

// 중첩된 sealed 클래스 계층 구조
public abstract sealed class Vehicle permits Car, Motorcycle, Truck {
    private final String registrationNumber;
    
    public Vehicle(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public abstract int getWheelCount();
}
