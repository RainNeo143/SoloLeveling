// MessageDao.java
package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.sololeveling.models.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    void insert(Message message);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    // Получить все сообщения между двумя пользователями
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) " +
            "OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    List<Message> getConversation(int userId1, int userId2);

    // Получить все уникальные чаты пользователя
    @Query("SELECT DISTINCT " +
            "CASE WHEN senderId = :userId THEN receiverId ELSE senderId END as otherUserId " +
            "FROM messages WHERE senderId = :userId OR receiverId = :userId " +
            "ORDER BY timestamp DESC")
    List<Integer> getConversationPartners(int userId);

    // Получить последнее сообщение в диалоге
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) " +
            "OR (senderId = :userId2 AND receiverId = :userId1) " +
            "ORDER BY timestamp DESC LIMIT 1")
    Message getLastMessage(int userId1, int userId2);

    // Получить количество непрочитанных сообщений от конкретного пользователя
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND senderId = :senderId AND isRead = 0")
    int getUnreadMessagesCount(int userId, int senderId);

    // Получить все непрочитанные сообщения пользователя
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    int getTotalUnreadMessages(int userId);

    // Отметить сообщения как прочитанные
    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId")
    void markConversationAsRead(int userId, int senderId);
}
