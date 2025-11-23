package com.example.sololeveling.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.models.UserQuestProgress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgressActivity extends AppCompatActivity {

    private ImageView ivBack, ivMenu;
    private LinearLayout llProgressGrid;

    private AppDatabase database;
    private User currentUser;
    private ExecutorService executorService;

    private Map<String, CardView> questCards = new HashMap<>();
    private Map<String, TextView> progressTexts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
        loadCurrentUser();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivMenu = findViewById(R.id.ivMenu);
        llProgressGrid = findViewById(R.id.llProgressGrid);

        // Создаем карточки для каждого квеста
        createQuestCards();
    }

    private void createQuestCards() {
        String[] questNames = {"Качалка", "Бег", "Финансовая грамотность", "Рисование", "Программирование", "Кулинария"};
        int[] iconIds = {R.drawable.ic_gym, R.drawable.ic_run, R.drawable.ic_finance,
                R.drawable.ic_art, R.drawable.ic_code, R.drawable.ic_cook};

        for (int i = 0; i < questNames.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
            rowParams.setMargins(0, 0, 0, dpToPx(16));
            row.setLayoutParams(rowParams);

            for (int j = 0; j < 2 && (i + j) < questNames.length; j++) {
                int index = i + j;
                CardView card = createQuestCard(questNames[index], iconIds[index]);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

                if (j == 0) {
                    cardParams.setMargins(0, 0, dpToPx(8), 0);
                } else {
                    cardParams.setMargins(dpToPx(8), 0, 0, 0);
                }

                card.setLayoutParams(cardParams);
                row.addView(card);

                questCards.put(questNames[index], card);
            }

            llProgressGrid.addView(row);
        }
    }

    private CardView createQuestCard(String questName, int iconId) {
        CardView card = new CardView(this);
        card.setCardElevation(dpToPx(4));
        card.setRadius(dpToPx(12));
        card.setCardBackgroundColor(getResources().getColor(R.color.white));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(android.view.Gravity.CENTER);
        content.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconId);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48));
        iconParams.setMargins(0, 0, 0, dpToPx(8));
        icon.setLayoutParams(iconParams);
        content.addView(icon);

        TextView name = new TextView(this);
        name.setText(questName);
        name.setTextColor(getResources().getColor(R.color.dark_gray));
        name.setTextSize(14);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        name.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 0, 0, dpToPx(4));
        name.setLayoutParams(nameParams);
        content.addView(name);

        TextView progressLabel = new TextView(this);
        progressLabel.setText("Прогресс:");
        progressLabel.setTextColor(getResources().getColor(R.color.dark_gray));
        progressLabel.setTextSize(12);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 0, 0, dpToPx(2));
        progressLabel.setLayoutParams(labelParams);
        content.addView(progressLabel);

        TextView progress = new TextView(this);
        progress.setText("0%");
        progress.setTextColor(getResources().getColor(R.color.red_accent));
        progress.setTextSize(16);
        progress.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(progress);

        progressTexts.put(questName, progress);

        card.addView(content);
        return card;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadCurrentUser() {
        executorService.execute(() -> {
            SharedPreferences prefs = getSharedPreferences("SoloLevelingPrefs", MODE_PRIVATE);
            int userId = getIntent().getIntExtra("userId", -1);

            if (userId == -1) {
                String login = prefs.getString("login", "");
                if (!login.isEmpty()) {
                    currentUser = database.userDao().getUserByLogin(login);
                }
            } else {
                currentUser = database.userDao().getUserById(userId);
            }

            if (currentUser != null) {
                loadProgress();
            }
        });
    }

    private void loadProgress() {
        executorService.execute(() -> {
            List<Quest> quests = database.questDao().getAllQuests();

            for (Quest quest : quests) {
                int totalLessons = database.lessonDao().getTotalLessonsCount(quest.getId());
                int completedLessons = database.lessonDao().getCompletedLessonsCount(quest.getId());

                int percentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

                final String questName = quest.getName();
                final String progressText = percentage + "%";

                runOnUiThread(() -> {
                    TextView progressView = progressTexts.get(questName);
                    if (progressView != null) {
                        progressView.setText(progressText);
                    }
                });
            }
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Меню", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadProgress();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}