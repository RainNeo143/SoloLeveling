package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sololeveling.models.Quest;

import java.util.List;

@Dao
public interface QuestDao {
    @Insert
    void insert(Quest quest);

    @Update
    void update(Quest quest);

    @Delete
    void delete(Quest quest);

    @Query("SELECT * FROM quests")
    List<Quest> getAllQuests();

    @Query("SELECT * FROM quests WHERE category = :category")
    List<Quest> getQuestsByCategory(String category);

    @Query("SELECT * FROM quests WHERE isFavorite = 1")
    List<Quest> getFavoriteQuests();
}