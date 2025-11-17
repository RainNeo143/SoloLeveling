package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sololeveling.models.UserQuestProgress;

import java.util.List;

@Dao
public interface UserQuestProgressDao {
    @Insert
    void insert(UserQuestProgress progress);

    @Update
    void update(UserQuestProgress progress);

    @Query("SELECT * FROM user_quest_progress WHERE userId = :userId AND questId = :questId LIMIT 1")
    UserQuestProgress getProgress(int userId, int questId);

    @Query("SELECT * FROM user_quest_progress WHERE userId = :userId")
    List<UserQuestProgress> getAllUserProgress(int userId);

    @Query("SELECT * FROM user_quest_progress WHERE userId = :userId AND isActive = 1")
    List<UserQuestProgress> getActiveQuests(int userId);
}