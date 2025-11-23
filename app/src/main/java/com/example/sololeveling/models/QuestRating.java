package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "quest_ratings",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Quest.class,
                        parentColumns = "id",
                        childColumns = "questId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = {"userId", "questId"}, unique = true)})
public class QuestRating {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int questId;
    private int rating; // от 1 до 5
    private long timestamp;

    public QuestRating(int userId, int questId, int rating, long timestamp) {
        this.userId = userId;
        this.questId = questId;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}