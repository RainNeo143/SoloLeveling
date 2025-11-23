// NotificationDao.java
package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.sololeveling.models.Notification;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Delete
    void delete(Notification notification);

    // Получить все уведомления пользователя
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    List<Notification> getAllNotifications(int userId);

    // Получить количество непрочитанных уведомлений
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadNotificationsCount(int userId);

    // Отметить уведомление как прочитанное
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    void markAsRead(int notificationId);

    // Отметить все уведомления как прочитанные
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllAsRead(int userId);

    // Удалить все уведомления пользователя
    @Query("DELETE FROM notifications WHERE userId = :userId")
    void deleteAllNotifications(int userId);
}