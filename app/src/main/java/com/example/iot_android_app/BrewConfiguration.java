package com.example.iot_android_app;

public class BrewConfiguration {
    private int coffeeId; //1to3
    private int shotSize; // 1 to 3
    private int sugarLevel; //0 to 3
    private int temperature; // in °C 60 to 90

    // Constructor, getters and setters
    public BrewConfiguration(int coffeeId, int shotSize, int sugarLevel, int temperature) {
        this.coffeeId = coffeeId;
        this.shotSize = shotSize;
        this.sugarLevel = sugarLevel;
        this.temperature = temperature;
    }

    // Getters and setters...
    public void setCoffeeId(int coffeeId) {
        this.coffeeId = coffeeId;
    }

    public void setShotSize(int shotSize) {
        this.shotSize = shotSize;
    }

    public void setSugarLevel(int sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
