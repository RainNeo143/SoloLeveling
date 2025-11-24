package com.example.sololeveling.utils;

import android.content.Context;

import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.DailyReminder;
import com.example.sololeveling.models.Notification;
import com.example.sololeveling.models.Quest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderManager {

    private final AppDatabase database;
    private final ExecutorService executorService;

    public ReminderManager(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Создать напоминание для квеста
     */
    public void createReminder(int userId, int questId) {
        executorService.execute(() -> {
            DailyReminder existing = database.dailyReminderDao()
                    .getReminder(userId, questId);

            if (existing == null) {
                DailyReminder reminder = new DailyReminder(userId, questId);
                database.dailyReminderDao().insert(reminder);
            }
        });
    }

    /**
     * Обновить время последнего урока
     */
    public void updateLastLesson(int userId, int questId) {
        executorService.execute(() -> {
            long currentTime = System.currentTimeMillis();
            database.dailyReminderDao()
                    .updateLastLessonDate(userId, questId, currentTime);
            database.dailyReminderDao()
                    .resetMissedDays(userId, questId);
        });
    }

    /**
     * Проверить и отправить напоминания
     */
    public void checkAndSendReminders() {
        executorService.execute(() -> {
            List<DailyReminder> allReminders = database.dailyReminderDao()
                    .getAllActiveReminders();

            for (DailyReminder reminder : allReminders) {
                if (reminder.shouldSendReminder()) {
                    sendReminderNotification(reminder);
                }
            }
        });
    }

    /**
     * Отправить уведомление-напоминание
     */
    private void sendReminderNotification(DailyReminder reminder) {
        Quest quest = database.questDao().getAllQuests().stream()
                .filter(q -> q.getId() == reminder.getQuestId())
                .findFirst()
                .orElse(null);

        if (quest == null) return;

        String title = "⏰ Не забудьте про обучение!";
        String message = String.format(
                "Вы не проходили урок по курсу '%s' сегодня. " +
                        "Продолжайте развиваться! 💪",
                quest.getName()
        );

        Notification notification = new Notification(
                reminder.getUserId(),
                title,
                message,
                "daily_reminder",
                -1,
                quest.getId(),
                System.currentTimeMillis()
        );

        database.notificationDao().insert(notification);

        // Обновить дату отправки напоминания
        database.dailyReminderDao().updateReminderSentDate(
                reminder.getUserId(),
                reminder.getQuestId(),
                System.currentTimeMillis()
        );

        // Увеличить счетчик пропущенных дней
        database.dailyReminderDao().incrementMissedDays(
                reminder.getUserId(),
                reminder.getQuestId()
        );
    }

    /**
     * Деактивировать напоминание (когда квест завершен)
     */
    public void deactivateReminder(int userId, int questId) {
        executorService.execute(() -> {
            database.dailyReminderDao()
                    .deactivateReminder(userId, questId);
        });
    }

    /**
     * Получить статистику пропущенных дней
     */
    public void getMissedDaysStats(int userId, OnStatsLoadedListener listener) {
        executorService.execute(() -> {
            List<DailyReminder> reminders = database.dailyReminderDao()
                    .getActiveReminders(userId);

            int totalMissedDays = 0;
            for (DailyReminder reminder : reminders) {
                totalMissedDays += reminder.getMissedDaysCount();
            }

            int finalTotal = totalMissedDays;
            // Callback на UI thread можно добавить через Handler
            if (listener != null) {
                listener.onStatsLoaded(finalTotal);
            }
        });
    }

    public interface OnStatsLoadedListener {
        void onStatsLoaded(int missedDays);
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}