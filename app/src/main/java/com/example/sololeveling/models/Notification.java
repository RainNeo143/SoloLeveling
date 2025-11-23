// Notification.java
package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE))
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private String title;
    private String message;
    private String type; // "message", "comment", "like", "quest_complete"
    private int relatedUserId; // ID пользователя, который отправил уведомление
    private int relatedQuestId; // ID квеста (если применимо)
    private long timestamp;
    private boolean isRead;

    public Notification(int userId, String title, String message, String type,
                        int relatedUserId, int relatedQuestId, long timestamp) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedUserId = relatedUserId;
        this.relatedQuestId = relatedQuestId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(int relatedUserId) { this.relatedUserId = relatedUserId; }

    public int getRelatedQuestId() { return relatedQuestId; }
    public void setRelatedQuestId(int relatedQuestId) { this.relatedQuestId = relatedQuestId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}