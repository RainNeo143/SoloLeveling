package com.example.sololeveling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.adapters.LessonAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Lesson;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestDetailActivity extends AppCompatActivity implements LessonAdapter.OnLessonClickListener {

    private TextView tvQuestTitle, tvProgressPercentage, tvCompletedLessons, tvReward, tvRatingDetail;
    private ProgressBar progressBar;
    private ImageView ivBackDetail, ivFavoriteDetail, ivQuestIconDetail;
    private Button btnStartQuest;
    private RecyclerView rvLessons;

    private LessonAdapter lessonAdapter;
    private AppDatabase database;
    private Quest quest;
    private User currentUser;
    private UserQuestProgress progress;
    private List<Lesson> lessons;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_detail);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupRecyclerView();
        loadData();
        setupListeners();
    }

    private void initViews() {
        tvQuestTitle = findViewById(R.id.tvQuestTitle);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvCompletedLessons = findViewById(R.id.tvCompletedLessons);
        tvReward = findViewById(R.id.tvReward);
        tvRatingDetail = findViewById(R.id.tvRatingDetail);
        progressBar = findViewById(R.id.progressBar);
        ivBackDetail = findViewById(R.id.ivBackDetail);
        ivFavoriteDetail = findViewById(R.id.ivFavoriteDetail);
        ivQuestIconDetail = findViewById(R.id.ivQuestIconDetail);
        btnStartQuest = findViewById(R.id.btnStartQuest);
        rvLessons = findViewById(R.id.rvLessons);
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(this);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
    }

    private void loadData() {
        int questId = getIntent().getIntExtra("questId", -1);
        int userId = getIntent().getIntExtra("userId", -1);

        if (questId == -1 || userId == -1) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ИСПРАВЛЕНО: Загрузка данных в фоновом потоке
        executorService.execute(() -> {
            // Загрузка данных из БД
            List<Quest> allQuests = database.questDao().getAllQuests();
            quest = null;
            for (Quest q : allQuests) {
                if (q.getId() == questId) {
                    quest = q;
                    break;
                }
            }

            if (quest == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Квест не найден", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            currentUser = database.userDao().getUserById(userId);
            lessons = database.lessonDao().getLessonsByQuestId(questId);

            // Получить или создать прогресс
            progress = database.userQuestProgressDao().getProgress(userId, questId);
            if (progress == null) {
                progress = new UserQuestProgress(userId, questId, lessons.size());
                database.userQuestProgressDao().insert(progress);
                progress = database.userQuestProgressDao().getProgress(userId, questId);
            }

            // Обновление UI в главном потоке
            runOnUiThread(() -> {
                lessonAdapter.setLessons(lessons);
                updateUI();
            });
        });
    }

    private void setupListeners() {
        ivBackDetail.setOnClickListener(v -> finish());

        ivFavoriteDetail.setOnClickListener(v -> {
            // ИСПРАВЛЕНО: Обновление в фоновом потоке
            executorService.execute(() -> {
                quest.setFavorite(!quest.isFavorite());
                database.questDao().update(quest);

                runOnUiThread(() -> {
                    updateFavoriteIcon();
                    Toast.makeText(this, quest.isFavorite() ? "Добавлено в избранное" : "Удалено из избранного",
                            Toast.LENGTH_SHORT).show();
                });
            });
        });

        btnStartQuest.setOnClickListener(v -> {
            executorService.execute(() -> {
                if (!progress.isActive()) {
                    progress.setActive(true);
                    database.userQuestProgressDao().update(progress);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Квест активирован!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    });
                }

                // Найти следующий доступный урок
                Lesson nextLesson = database.lessonDao().getNextIncompleteLesson(quest.getId());
                runOnUiThread(() -> {
                    if (nextLesson != null) {
                        openLesson(nextLesson);
                    } else {
                        Toast.makeText(this, "Все уроки завершены!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void updateUI() {
        if (quest == null) return;

        tvQuestTitle.setText(quest.getName());
        tvReward.setText("$ " + quest.getReward());
        tvRatingDetail.setText(String.valueOf(quest.getRating()));

        // Установить иконку квеста
        setQuestIcon();
        updateFavoriteIcon();

        // ИСПРАВЛЕНО: Обновить прогресс в фоновом потоке
        executorService.execute(() -> {
            int completed = database.lessonDao().getCompletedLessonsCount(quest.getId());
            int total = lessons.size();
            int percentage = total > 0 ? (completed * 100) / total : 0;

            progress.setCompletedLessons(completed);
            progress.setTotalLessons(total);
            database.userQuestProgressDao().update(progress);

            int finalCompleted = completed;
            int finalPercentage = percentage;
            runOnUiThread(() -> {
                tvProgressPercentage.setText(finalPercentage + "%");
                tvCompletedLessons.setText("Выполнено: " + finalCompleted + "/" + total + " уроков");
                progressBar.setProgress(finalPercentage);

                // Обновить текст кнопки
                if (progress.isActive()) {
                    if (finalCompleted == total) {
                        btnStartQuest.setText("КВЕСТ ЗАВЕРШЁН");
                        btnStartQuest.setEnabled(false);
                    } else {
                        btnStartQuest.setText("ПРОДОЛЖИТЬ");
                    }
                } else {
                    btnStartQuest.setText("НАЧАТЬ КВЕСТ");
                }
            });
        });
    }

    private void setQuestIcon() {
        String iconName = quest.getIcon();
        int iconRes = R.drawable.ic_gym; // default

        switch (iconName) {
            case "gym":
                iconRes = R.drawable.ic_gym;
                break;
            case "run":
                iconRes = R.drawable.ic_run;
                break;
            case "finance":
                iconRes = R.drawable.ic_finance;
                break;
            case "art":
                iconRes = R.drawable.ic_art;
                break;
            case "code":
                iconRes = R.drawable.ic_code;
                break;
            case "cook":
                iconRes = R.drawable.ic_cook;
                break;
        }

        ivQuestIconDetail.setImageResource(iconRes);
    }

    private void updateFavoriteIcon() {
        if (quest.isFavorite()) {
            ivFavoriteDetail.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            ivFavoriteDetail.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public void onLessonClick(Lesson lesson, int position) {
        openLesson(lesson);
    }

    private void openLesson(Lesson lesson) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("lessonId", lesson.getId());
        intent.putExtra("questId", quest.getId());
        intent.putExtra("userId", currentUser.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновить данные при возврате из урока
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}