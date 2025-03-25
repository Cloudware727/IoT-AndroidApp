package com.example.iot_android_app;

public class orderModel {
    private String type;
    private int shots;
    private int sugar;
    private int temp;
    private String date;
    private boolean canBeOrdered;
    private boolean isFavorite;

    public orderModel(String date, String type, int shots, int sugar, int temp, boolean cbo, boolean fav) {
        this.type = type;
        this.date = date;
        this.shots = shots;
        this.sugar = sugar;
        this.temp = temp;
        canBeOrdered = cbo;
        isFavorite = fav;
    }

    @Override
    public String toString() {
        return date + '\n' + type + " (" + shots + " shot" + (shots == 1?")":"s)") +
                "\nSugar level: " + sugar + "\nTemperature: " + temp + "\u00B0C";
    }

    public int getShots() {
        return shots;
    }

    public int getSugar() {
        return sugar;
    }

    public int getTemp() {
        return temp;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public boolean CanBeOrdered() {
        return canBeOrdered;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getFavIcon() {
        return isFavorite ? R.drawable.remove_from_fav : R.drawable.history_add_fav;
    }

    public int getReIcon() {
        return canBeOrdered? R.drawable.redo_icon : R.drawable.redo_unav;
    }
}