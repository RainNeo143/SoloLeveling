package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_reminders",
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
public class DailyReminder {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int questId;
    private long lastLessonDate; // Дата последнего урока
    private long reminderSentDate; // Дата отправки напоминания
    private boolean isActive; // Активен ли квест
    private int missedDaysCount; // Количество пропущенных дней

    public DailyReminder(int userId, int questId) {
        this.userId = userId;
        this.questId = questId;
        this.lastLessonDate = System.currentTimeMillis();
        this.reminderSentDate = 0;
        this.isActive = true;
        this.missedDaysCount = 0;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public long getLastLessonDate() { return lastLessonDate; }
    public void setLastLessonDate(long lastLessonDate) {
        this.lastLessonDate = lastLessonDate;
    }

    public long getReminderSentDate() { return reminderSentDate; }
    public void setReminderSentDate(long reminderSentDate) {
        this.reminderSentDate = reminderSentDate;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getMissedDaysCount() { return missedDaysCount; }
    public void setMissedDaysCount(int missedDaysCount) {
        this.missedDaysCount = missedDaysCount;
    }

    // Вспомогательные методы
    public boolean shouldSendReminder() {
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;

        // Проверяем, прошло ли больше 18 часов с последнего урока
        long timeSinceLastLesson = currentTime - lastLessonDate;
        boolean moreThan18Hours = timeSinceLastLesson > (18 * 60 * 60 * 1000);

        // Проверяем, не отправляли ли напоминание сегодня
        boolean reminderSentToday = false;
        if (reminderSentDate > 0) {
            long timeSinceReminder = currentTime - reminderSentDate;
            reminderSentToday = timeSinceReminder < dayInMillis;
        }

        return isActive && moreThan18Hours && !reminderSentToday;
    }
}