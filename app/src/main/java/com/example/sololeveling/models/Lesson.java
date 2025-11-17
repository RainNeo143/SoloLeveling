package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons",
        foreignKeys = @ForeignKey(entity = Quest.class,
                parentColumns = "id",
                childColumns = "questId",
                onDelete = ForeignKey.CASCADE))
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int questId;
    private String title;
    private String description;
    private int orderNumber;
    private int experienceReward;
    private boolean isCompleted;
    private String type; // "daily", "weekly", "challenge"

    public Lesson(int questId, String title, String description, int orderNumber, int experienceReward, String type) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.orderNumber = orderNumber;
        this.experienceReward = experienceReward;
        this.type = type;
        this.isCompleted = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    public int getExperienceReward() { return experienceReward; }
    public void setExperienceReward(int experienceReward) { this.experienceReward = experienceReward; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}