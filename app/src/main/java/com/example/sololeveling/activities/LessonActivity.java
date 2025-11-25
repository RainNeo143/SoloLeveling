package com.example.sololeveling.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Lesson;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LessonActivity extends AppCompatActivity {

    private TextView tvLessonNumber, tvLessonStatus, tvLessonTitleDetail;
    private TextView tvLessonTypeDetail, tvLessonDescriptionDetail, tvLessonReward;
    private TextView tvCompletedMessage, tvLessonTip;
    private ImageView ivBackLesson, ivLessonTypeIcon;
    private Button btnCompleteLesson, btnStartTest;

    private AppDatabase database;
    private Lesson lesson;
    private Quest quest;
    private User currentUser;
    private int totalLessons;
    private int currentLessonNumber;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        loadData();
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
        btnStartTest = findViewById(R.id.btnStartTest);
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

        executorService.execute(() -> {
            List<Lesson> allLessons = database.lessonDao().getLessonsByQuestId(questId);
            lesson = null;
            for (Lesson l : allLessons) {
                if (l.getId() == lessonId) {
                    lesson = l;
                    break;
                }
            }

            if (lesson == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Урок не найден", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

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

            runOnUiThread(this::updateUI);
        });
    }

    private void updateUI() {
        if (lesson == null) return;

        tvLessonNumber.setText("Урок " + currentLessonNumber + "/" + totalLessons);
        tvLessonTitleDetail.setText(lesson.getTitle());
        tvLessonDescriptionDetail.setText(lesson.getDescription());
        tvLessonReward.setText("+" + lesson.getExperienceReward() + " XP");

        setLessonType();

        if (lesson.isCompleted()) {
            tvLessonStatus.setText("ЗАВЕРШЁН");
            tvLessonStatus.setBackgroundResource(R.drawable.badge_completed);
            btnCompleteLesson.setVisibility(View.GONE);
            tvCompletedMessage.setVisibility(View.VISIBLE);

            // Показать кнопку теста если он доступен
            if (!lesson.getTestQuestion().isEmpty() && !lesson.isTestPassed()) {
                btnStartTest.setVisibility(View.VISIBLE);
            }
        } else {
            tvLessonStatus.setText("НЕ ЗАВЕРШЁН");
            tvLessonStatus.setBackgroundResource(R.drawable.badge_incomplete);
            btnCompleteLesson.setVisibility(View.VISIBLE);
            tvCompletedMessage.setVisibility(View.GONE);
            btnStartTest.setVisibility(View.GONE);
        }

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

        btnStartTest.setOnClickListener(v -> showTestDialog());
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
        executorService.execute(() -> {
            lesson.setCompleted(true);
            lesson.setCompletedDate(System.currentTimeMillis());
            database.lessonDao().update(lesson);

            int newExperience = currentUser.getExperience() + lesson.getExperienceReward();
            currentUser.setExperience(newExperience);

            int oldLevel = currentUser.getLevel();
            currentUser.calculateLevel();
            int newLevel = currentUser.getLevel();

            database.userDao().update(currentUser);

            UserQuestProgress progress = database.userQuestProgressDao()
                    .getProgress(currentUser.getId(), quest.getId());
            if (progress != null) {
                int completed = database.lessonDao().getCompletedLessonsCount(quest.getId());
                progress.setCompletedLessons(completed);
                database.userQuestProgressDao().update(progress);
            }

            runOnUiThread(() -> {
                showRewardDialog(oldLevel, newLevel);
            });
        });
    }

    private void showRewardDialog(int oldLevel, int newLevel) {
        String message = "Вы получили " + lesson.getExperienceReward() + " XP!";

        if (newLevel > oldLevel) {
            message += "\n\n🎉 ПОЗДРАВЛЯЕМ!\nВы достигли " + newLevel + " уровня!";
        }

        String finalMessage1 = message;
        executorService.execute(() -> {
            int completed = database.lessonDao().getCompletedLessonsCount(quest.getId());

            String finalMessage = finalMessage1;
            if (completed == totalLessons) {
                finalMessage += "\n\n⭐ Квест полностью завершён!";
            }

            String displayMessage = finalMessage;
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("🏆 Урок завершён!")
                        .setMessage(displayMessage)
                        .setPositiveButton("Отлично!", (dialog, which) -> {
                            animateReward();
                            updateUI();
                        })
                        .setCancelable(false)
                        .show();
            });
        });
    }

    private void showTestDialog() {
        if (lesson.getTestQuestion().isEmpty()) {
            Toast.makeText(this, "Для этого урока нет теста", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_test, null);

        TextView tvTestQuestion = dialogView.findViewById(R.id.tvTestQuestion);
        RadioGroup rgAnswers = dialogView.findViewById(R.id.rgAnswers);
        RadioButton rbOption1 = dialogView.findViewById(R.id.rbOption1);
        RadioButton rbOption2 = dialogView.findViewById(R.id.rbOption2);
        RadioButton rbOption3 = dialogView.findViewById(R.id.rbOption3);
        RadioButton rbOption4 = dialogView.findViewById(R.id.rbOption4);
        TextView tvAttemptsInfo = dialogView.findViewById(R.id.tvAttemptsInfo);

        tvTestQuestion.setText(lesson.getTestQuestion());
        rbOption1.setText(lesson.getTestOption1());
        rbOption2.setText(lesson.getTestOption2());
        rbOption3.setText(lesson.getTestOption3());
        rbOption4.setText(lesson.getTestOption4());

        int attemptsLeft = 3 - lesson.getAttemptsCount();
        tvAttemptsInfo.setText("Попытка " + (lesson.getAttemptsCount() + 1) + " из 3");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnCancelTest).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSubmitTest).setOnClickListener(v -> {
            int selectedId = rgAnswers.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedAnswer = 0;
            if (selectedId == R.id.rbOption1) selectedAnswer = 1;
            else if (selectedId == R.id.rbOption2) selectedAnswer = 2;
            else if (selectedId == R.id.rbOption3) selectedAnswer = 3;
            else if (selectedId == R.id.rbOption4) selectedAnswer = 4;

            checkTestAnswer(selectedAnswer, dialog);
        });

        dialog.show();
    }

    private void checkTestAnswer(int selectedAnswer, AlertDialog dialog) {
        executorService.execute(() -> {
            lesson.setAttemptsCount(lesson.getAttemptsCount() + 1);

            boolean isCorrect = selectedAnswer == lesson.getCorrectAnswerIndex();

            if (isCorrect) {
                lesson.setTestPassed(true);
                database.lessonDao().update(lesson);

                runOnUiThread(() -> {
                    dialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("✅ Правильно!")
                            .setMessage("Отличная работа! Вы прошли тест.")
                            .setPositiveButton("OK", (d, w) -> updateUI())
                            .show();
                });
            } else {
                database.lessonDao().update(lesson);

                int attemptsLeft = 3 - lesson.getAttemptsCount();

                runOnUiThread(() -> {
                    if (attemptsLeft > 0) {
                        dialog.dismiss();
                        new AlertDialog.Builder(this)
                                .setTitle("❌ Неправильно")
                                .setMessage("Попробуйте еще раз. Осталось попыток: " + attemptsLeft)
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        dialog.dismiss();
                        new AlertDialog.Builder(this)
                                .setTitle("Попытки закончились")
                                .setMessage("К сожалению, вы использовали все попытки. Повторите урок позже.")
                                .setPositiveButton("OK", (d, w) -> updateUI())
                                .show();
                    }
                });
            }
        });
    }

    private void animateReward() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvLessonReward, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvLessonReward, "scaleY", 1f, 1.3f, 1f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.start();
        scaleY.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}