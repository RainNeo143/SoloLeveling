package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import com.example.sololeveling.models.QuestRating;

@Dao
public interface QuestRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QuestRating rating);

    @Update
    void update(QuestRating rating);

    // Получить оценку пользователя для квеста
    @Query("SELECT * FROM quest_ratings WHERE userId = :userId AND questId = :questId LIMIT 1")
    QuestRating getUserRating(int userId, int questId);

    // Получить среднюю оценку квеста
    @Query("SELECT AVG(rating) FROM quest_ratings WHERE questId = :questId")
    Float getAverageRating(int questId);

    // Получить количество оценок
    @Query("SELECT COUNT(*) FROM quest_ratings WHERE questId = :questId")
    int getRatingsCount(int questId);
}