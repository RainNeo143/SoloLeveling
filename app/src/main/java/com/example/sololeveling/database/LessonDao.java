package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sololeveling.models.Lesson;

import java.util.List;

@Dao
public interface LessonDao {
    @Insert
    void insert(Lesson lesson);

    @Update
    void update(Lesson lesson);

    @Delete
    void delete(Lesson lesson);

    @Query("SELECT * FROM lessons WHERE questId = :questId ORDER BY orderNumber ASC")
    List<Lesson> getLessonsByQuestId(int questId);

    @Query("SELECT * FROM lessons WHERE questId = :questId AND isCompleted = 0 ORDER BY orderNumber ASC LIMIT 1")
    Lesson getNextIncompleteLesson(int questId);

    @Query("SELECT COUNT(*) FROM lessons WHERE questId = :questId")
    int getTotalLessonsCount(int questId);

    @Query("SELECT COUNT(*) FROM lessons WHERE questId = :questId AND isCompleted = 1")
    int getCompletedLessonsCount(int questId);
}