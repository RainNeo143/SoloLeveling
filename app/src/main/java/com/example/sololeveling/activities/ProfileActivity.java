package com.example.sololeveling.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.utils.ImageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar, ivBack;
    private EditText etNickname, etCity, etBio;
    private Button btnChangeAvatar, btnSaveProfile, btnChangePassword;
    private LinearLayout llProgressSection;
    private TextView tvProfileLevel, tvProfileExperience;

    private AppDatabase database;
    private User currentUser;
    private ExecutorService executorService;
    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Map<String, CardView> questCards = new HashMap<>();
    private Map<String, TextView> progressTexts = new HashMap<>();
    private Map<String, ProgressBar> progressBars = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
        checkPermissions();
        loadUserProfile();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        ivBack = findViewById(R.id.ivBack);
        etNickname = findViewById(R.id.etNickname);
        etCity = findViewById(R.id.etCity);
        etBio = findViewById(R.id.etBio);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        llProgressSection = findViewById(R.id.llProgressSection);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvProfileExperience = findViewById(R.id.tvProfileExperience);

        createQuestProgressCards();
    }

    private void createQuestProgressCards() {
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
                CardView card = createQuestProgressCard(questNames[index], iconIds[index]);

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

            llProgressSection.addView(row);
        }
    }

    private CardView createQuestProgressCard(String questName, int iconId) {
        CardView card = new CardView(this);
        card.setCardElevation(dpToPx(4));
        card.setRadius(dpToPx(12));
        card.setCardBackgroundColor(getResources().getColor(R.color.white));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(android.view.Gravity.CENTER);
        content.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconId);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        iconParams.setMargins(0, 0, 0, dpToPx(8));
        icon.setLayoutParams(iconParams);
        content.addView(icon);

        TextView name = new TextView(this);
        name.setText(questName);
        name.setTextColor(getResources().getColor(R.color.dark_gray));
        name.setTextSize(12);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        name.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 0, 0, dpToPx(8));
        name.setLayoutParams(nameParams);
        content.addView(name);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        LinearLayout.LayoutParams progressBarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(8)
        );
        progressBarParams.setMargins(0, 0, 0, dpToPx(4));
        progressBar.setLayoutParams(progressBarParams);
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_drawable));
        content.addView(progressBar);
        progressBars.put(questName, progressBar);

        TextView progress = new TextView(this);
        progress.setText("0%");
        progress.setTextColor(getResources().getColor(R.color.red_accent));
        progress.setTextSize(14);
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

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Функция в разработке", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
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
                runOnUiThread(() -> {
                    etNickname.setText(currentUser.getNickname());
                    etCity.setText(currentUser.getCity());
                    etBio.setText(currentUser.getBio());
                    tvProfileLevel.setText("Уровень: " + currentUser.getLevel());
                    tvProfileExperience.setText("Опыт: " + currentUser.getExperience() + " XP");

                    // ИСПРАВЛЕНО: Безопасная загрузка аватара
                    ImageUtils.loadAvatar(ProfileActivity.this, currentUser.getAvatarPath(), ivProfileAvatar);
                });

                loadProgress();
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                    finish();
                });
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
                final int progressValue = percentage;

                runOnUiThread(() -> {
                    TextView progressView = progressTexts.get(questName);
                    ProgressBar progressBar = progressBars.get(questName);

                    if (progressView != null) {
                        progressView.setText(progressText);
                    }
                    if (progressBar != null) {
                        progressBar.setProgress(progressValue);
                    }
                });
            }
        });
    }

    private void saveProfile() {
        String nickname = etNickname.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (nickname.isEmpty()) {
            etNickname.setError("Введите ник");
            return;
        }

        if (bio.length() > 500) {
            etBio.setError("Максимум 500 символов");
            return;
        }

        executorService.execute(() -> {
            currentUser.setNickname(nickname);
            currentUser.setCity(city);
            currentUser.setBio(bio);
            database.userDao().update(currentUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            if (selectedImage != null && currentUser != null) {
                // ИСПРАВЛЕНО: Сохраняем изображение во внутреннее хранилище
                executorService.execute(() -> {
                    String savedPath = ImageUtils.saveImageToInternalStorage(
                            ProfileActivity.this,
                            selectedImage,
                            currentUser.getId()
                    );

                    if (savedPath != null) {
                        currentUser.setAvatarPath(savedPath);
                        database.userDao().update(currentUser);

                        runOnUiThread(() -> {
                            ImageUtils.loadAvatar(ProfileActivity.this, savedPath, ivProfileAvatar);
                            Toast.makeText(this, "Аватар обновлен", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Ошибка сохранения аватара", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
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