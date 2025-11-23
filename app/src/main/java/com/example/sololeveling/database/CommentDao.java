package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.sololeveling.models.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insert(Comment comment);

    @Delete
    void delete(Comment comment);

    // Получить все основные комментарии для квеста
    @Query("SELECT * FROM comments WHERE questId = :questId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    List<Comment> getMainComments(int questId);

    // Получить все ответы на комментарий
    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY timestamp ASC")
    List<Comment> getReplies(int parentCommentId);

    // Получить количество комментариев к квесту
    @Query("SELECT COUNT(*) FROM comments WHERE questId = :questId")
    int getCommentsCount(int questId);

    // Получить комментарий по ID
    @Query("SELECT * FROM comments WHERE id = :commentId LIMIT 1")
    Comment getCommentById(int commentId);
}