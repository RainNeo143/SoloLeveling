package com.example.sololeveling.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Lesson;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;

import java.util.List;

public class LessonActivity extends AppCompatActivity {

    private TextView tvLessonNumber, tvLessonStatus, tvLessonTitleDetail;
    private TextView tvLessonTypeDetail, tvLessonDescriptionDetail, tvLessonReward;
    private TextView tvCompletedMessage, tvLessonTip;
    private ImageView ivBackLesson, ivLessonTypeIcon;
    private Button btnCompleteLesson;

    private AppDatabase database;
    private Lesson lesson;
    private Quest quest;
    private User currentUser;
    private int totalLessons;
    private int currentLessonNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        database = AppDatabase.getInstance(this);

        initViews();
        loadData();
        updateUI();
        setupListeners();
    }

    private void initViews() {
        tvLessonNumber = findViewById(R.id.tvLessonNumber);
        tvLessonStatus = findViewById(R.id.tvLessonStatus);
        tvLessonTitleDetail = findViewById(R.id.tvLessonTitleDetail);
        tvLessonTypeDetail = findViewById(R.id.tvLessonTypeDetail);
        tvLessonDescriptionDetail = findViewById(R.id.tvLessonDescriptionDetail);
        tvLessonReward = findViewById(R.id.tvLessonReward);
        tvCompletedMessage = findViewById(R.id.tvCompletedMessage);
        tvLessonTip = findViewById(R.id.tvLessonTip);
        ivBackLesson = findViewById(R.id.ivBackLesson);
        ivLessonTypeIcon = findViewById(R.id.ivLessonTypeIcon);
        btnCompleteLesson = findViewById(R.id.btnCompleteLesson);
    }

    private void loadData() {
        int lessonId = getIntent().getIntExtra("lessonId", -1);
        int questId = getIntent().getIntExtra("questId", -1);
        int userId = getIntent().getIntExtra("userId", -1);

        if (lessonId == -1 || questId == -1 || userId == -1) {
            Toast.makeText(this, "Ошибка загрузки урока", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Загрузка урока
        List<Lesson> allLessons = database.lessonDao().getLessonsByQuestId(questId);
        lesson = null;
        for (Lesson l : allLessons) {
            if (l.getId() == lessonId) {
                lesson = l;
                break;
            }
        }

        if (lesson == null) {
            Toast.makeText(this, "Урок не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Загрузка квеста
        List<Quest> allQuests = database.questDao().getAllQuests();
        quest = null;
        for (Quest q : allQuests) {
            if (q.getId() == questId) {
                quest = q;
                break;
            }
        }

        currentUser = database.userDao().getUserById(userId);
        totalLessons = database.lessonDao().getTotalLessonsCount(questId);
        currentLessonNumber = lesson.getOrderNumber();
    }

    private void updateUI() {
        if (lesson == null) return;

        // Номер урока
        tvLessonNumber.setText("Урок " + currentLessonNumber + "/" + totalLessons);

        // Заголовок и описание
        tvLessonTitleDetail.setText(lesson.getTitle());
        tvLessonDescriptionDetail.setText(lesson.getDescription());

        // Награда
        tvLessonReward.setText("+" + lesson.getExperienceReward() + " XP");

        // Тип урока
        setLessonType();

        // Статус
        if (lesson.isCompleted()) {
            tvLessonStatus.setText("ЗАВЕРШЁН");
            tvLessonStatus.setBackgroundResource(R.drawable.badge_completed);
            btnCompleteLesson.setVisibility(View.GONE);
            tvCompletedMessage.setVisibility(View.VISIBLE);
        } else {
            tvLessonStatus.setText("НЕ ЗАВЕРШЁН");
            tvLessonStatus.setBackgroundResource(R.drawable.badge_incomplete);
            btnCompleteLesson.setVisibility(View.VISIBLE);
            tvCompletedMessage.setVisibility(View.GONE);
        }

        // Совет
        setLessonTip();
    }

    private void setLessonType() {
        switch (lesson.getType()) {
            case "daily":
                tvLessonTypeDetail.setText("Ежедневный урок");
                ivLessonTypeIcon.setImageResource(R.drawable.ic_daily);
                break;
            case "weekly":
                tvLessonTypeDetail.setText("Еженедельный урок");
                ivLessonTypeIcon.setImageResource(R.drawable.ic_weekly);
                break;
            case "challenge":
                tvLessonTypeDetail.setText("Испытание");
                ivLessonTypeIcon.setImageResource(R.drawable.ic_challenge);
                break;
        }
    }

    private void setLessonTip() {
        if (quest == null) return;

        String tip = "";
        switch (quest.getName()) {
            case "Качалка":
                tip = "Не спешите! Главное - правильная техника выполнения, а не скорость.";
                break;
            case "Бег":
                tip = "Начинайте с разминки и не забывайте пить воду до и после тренировки.";
                break;
            case "Финансовая грамотность":
                tip = "Записывайте все изученное и применяйте на практике постепенно.";
                break;
            case "Рисование":
                tip = "Практикуйтесь каждый день, даже если это всего 15 минут.";
                break;
            case "Программирование":
                tip = "Пишите код сами, не копируйте. Ошибки - это часть обучения!";
                break;
            case "Кулинария":
                tip = "Читайте рецепт полностью перед началом и подготовьте все ингредиенты.";
                break;
        }
        tvLessonTip.setText(tip);
    }

    private void setupListeners() {
        ivBackLesson.setOnClickListener(v -> finish());

        btnCompleteLesson.setOnClickListener(v -> showCompletionDialog());
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Завершить урок?")
                .setMessage("Вы уверены, что выполнили все задания этого урока?")
                .setPositiveButton("Да, завершить", (dialog, which) -> completeLesson())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void completeLesson() {
        // Отметить урок как завершённый
        lesson.setCompleted(true);
        database.lessonDao().update(lesson);

        // Добавить опыт пользователю
        int newExperience = currentUser.getExperience() + lesson.getExperienceReward();
        currentUser.setExperience(newExperience);

        // Проверить повышение уровня
        int oldLevel = currentUser.getLevel();
        currentUser.calculateLevel();
        int newLevel = currentUser.getLevel();

        database.userDao().update(currentUser);

        // Обновить прогресс квеста
        UserQuestProgress progress = database.userQuestProgressDao()
                .getProgress(currentUser.getId(), quest.getId());
        if (progress != null) {
            int completed = database.lessonDao().getCompletedLessonsCount(quest.getId());
            progress.setCompletedLessons(completed);
            database.userQuestProgressDao().update(progress);
        }

        // Показать диалог с наградой
        showRewardDialog(oldLevel, newLevel);
    }

    private void showRewardDialog(int oldLevel, int newLevel) {
        String message = "Вы получили " + lesson.getExperienceReward() + " XP!";

        if (newLevel > oldLevel) {
            message += "\n\n🎉 ПОЗДРАВЛЯЕМ!\nВы достигли " + newLevel + " уровня!";
        }

        // Проверить, все ли уроки завершены
        int completed = database.lessonDao().getCompletedLessonsCount(quest.getId());
        if (completed == totalLessons) {
            message += "\n\n⭐ Квест полностью завершён!";
        }

        new AlertDialog.Builder(this)
                .setTitle("🏆 Урок завершён!")
                .setMessage(message)
                .setPositiveButton("Отлично!", (dialog, which) -> {
                    // Анимация
                    animateReward();
                    // Обновить UI
                    updateUI();
                })
                .setCancelable(false)
                .show();
    }

    private void animateReward() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvLessonReward, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvLessonReward, "scaleY", 1f, 1.3f, 1f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.start();
        scaleY.start();
    }
}