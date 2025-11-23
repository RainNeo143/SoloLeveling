package com.example.sololeveling.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sololeveling.models.Lesson;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;
import com.example.sololeveling.models.Comment;
import com.example.sololeveling.models.QuestRating;
import com.example.sololeveling.models.Message;
import com.example.sololeveling.models.Notification;

@Database(entities = {
        Quest.class,
        User.class,
        Lesson.class,
        UserQuestProgress.class,
        Comment.class,
        QuestRating.class,
        Message.class,
        Notification.class
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract QuestDao questDao();
    public abstract UserDao userDao();
    public abstract LessonDao lessonDao();
    public abstract UserQuestProgressDao userQuestProgressDao();
    public abstract CommentDao commentDao();
    public abstract QuestRatingDao questRatingDao();
    public abstract MessageDao messageDao();
    public abstract NotificationDao notificationDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "solo_leveling_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}