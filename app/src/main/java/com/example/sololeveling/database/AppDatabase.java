package com.example.sololeveling.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sololeveling.models.Lesson;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;

@Database(entities = {Quest.class, User.class, Lesson.class, UserQuestProgress.class},
        version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract QuestDao questDao();
    public abstract UserDao userDao();
    public abstract LessonDao lessonDao();
    public abstract UserQuestProgressDao userQuestProgressDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "solo_leveling_database")
                    .fallbackToDestructiveMigration() // Удаляет старую БД при изменении версии
                    // ВАЖНО: Убрал allowMainThreadQueries() - это причина зависания!
                    // Все операции теперь должны выполняться в фоновом потоке
                    .build();
        }
        return instance;
    }

    // Метод для очистки БД (для тестирования)
    public static void destroyInstance() {
        instance = null;
    }
}