package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_quest_progress",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Quest.class,
                        parentColumns = "id",
                        childColumns = "questId",
                        onDelete = ForeignKey.CASCADE)
        })
public class UserQuestProgress {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int questId;
    private int completedLessons;
    private int totalLessons;
    private int progressPercentage;
    private boolean isActive;

    public UserQuestProgress(int userId, int questId, int totalLessons) {
        this.userId = userId;
        this.questId = questId;
        this.totalLessons = totalLessons;
        this.completedLessons = 0;
        this.progressPercentage = 0;
        this.isActive = false;
    }

    public void updateProgress() {
        if (totalLessons > 0) {
            this.progressPercentage = (completedLessons * 100) / totalLessons;
        }
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public int getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(int completedLessons) {
        this.completedLessons = completedLessons;
        updateProgress();
    }

    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
        updateProgress();
    }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}