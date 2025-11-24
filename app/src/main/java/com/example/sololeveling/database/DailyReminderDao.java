package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sololeveling.models.DailyReminder;

import java.util.List;

@Dao
public interface DailyReminderDao {

    @Insert
    void insert(DailyReminder reminder);

    @Update
    void update(DailyReminder reminder);

    // Получить напоминание для пользователя и квеста
    @Query("SELECT * FROM daily_reminders WHERE userId = :userId AND questId = :questId LIMIT 1")
    DailyReminder getReminder(int userId, int questId);

    // Получить все активные напоминания пользователя
    @Query("SELECT * FROM daily_reminders WHERE userId = :userId AND isActive = 1")
    List<DailyReminder> getActiveReminders(int userId);

    // Получить все напоминания, которые нужно отправить
    @Query("SELECT * FROM daily_reminders WHERE isActive = 1")
    List<DailyReminder> getAllActiveReminders();

    // Обновить дату последнего урока
    @Query("UPDATE daily_reminders SET lastLessonDate = :date WHERE userId = :userId AND questId = :questId")
    void updateLastLessonDate(int userId, int questId, long date);

    // Обновить дату отправки напоминания
    @Query("UPDATE daily_reminders SET reminderSentDate = :date WHERE userId = :userId AND questId = :questId")
    void updateReminderSentDate(int userId, int questId, long date);

    // Увеличить счетчик пропущенных дней
    @Query("UPDATE daily_reminders SET missedDaysCount = missedDaysCount + 1 WHERE userId = :userId AND questId = :questId")
    void incrementMissedDays(int userId, int questId);

    // Сбросить счетчик пропущенных дней
    @Query("UPDATE daily_reminders SET missedDaysCount = 0 WHERE userId = :userId AND questId = :questId")
    void resetMissedDays(int userId, int questId);

    // Деактивировать напоминание
    @Query("UPDATE daily_reminders SET isActive = 0 WHERE userId = :userId AND questId = :questId")
    void deactivateReminder(int userId, int questId);
}