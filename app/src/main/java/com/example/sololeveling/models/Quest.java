package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quests")
public class Quest {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String category;
    private int price;
    private float rating;
    private int progress;
    private boolean isFavorite;
    private String iconType;

    public Quest(String name, String category, int price, float rating, String iconType) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.progress = 0;
        this.isFavorite = false;
        this.iconType = iconType;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    // ТВОЙ ОШИБОЧНЫЙ МЕТОД — исправлено
    public String getIcon() {
        return iconType; // или вернуть путь к картинке, если хочешь
    }

    public String getReward() {
        return null; // тут вставь нужную логику
    }
}
