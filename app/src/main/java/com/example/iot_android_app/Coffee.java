package com.example.iot_android_app;

public class Coffee {
    private int id;
    private String name;
    private int coffeeLevel;
    private int imageResId;
    private String imagePath;

    public Coffee(int id, String name, int level, int imageResId) {
        this.id = id;
        this.name = name;
        this.coffeeLevel = level;
        this.imageResId = imageResId;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getCoffeeLevel() { return coffeeLevel; }
    public int getImageResId() { return imageResId; }

    public void setName(String name) { this.name = name; }
    public void setCoffeeLevel(int level) { this.coffeeLevel = level; }

    // Image URI (optional)
    // ✅ Image path
    public void setImagePath(String path) { this.imagePath = path; }
    public String getImagePath() { return imagePath; }
}
