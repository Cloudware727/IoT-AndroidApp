package com.example.iot_android_app;

public class Coffee {
    private int id;
    private String name;
    private int coffeeLevel; // Percentage of coffee remaining (0 to 100)
    private int bg;

    // Constructor
    public Coffee(int id, String name, int coffeeLevel, int bg) {
        this.id = id;
        this.name = name;
        this.coffeeLevel = coffeeLevel;
        this.bg = bg;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoffeeLevel() {
        return coffeeLevel;
    }

    public void setCoffeeLevel(int coffeeLevel) {
        this.coffeeLevel = coffeeLevel;
    }

    public int getBg() {
        return bg;
    }

    public void setBg(int bg) {
        this.bg = bg;
    }
}

