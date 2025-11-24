package com.example.sololeveling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.activities.ChatsListActivity;
import com.example.sololeveling.activities.NotificationsActivity;
import com.example.sololeveling.activities.ProgressActivity;
import com.example.sololeveling.activities.ProfileActivity;
import com.example.sololeveling.activities.QuestDetailActivity;
import com.example.sololeveling.adapters.QuestAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Quest;
import com.example.sololeveling.models.User;
import com.example.sololeveling.utils.LessonInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements QuestAdapter.OnQuestClickListener {

    private TextView tvNickname, tvSeeAll;
    private EditText etSearch;
    private ImageView ivAvatar, ivNotifications, ivFilter;
    private Button btnGoToPopular, btnMessagesMenu, btnProfileMenu;
    private RecyclerView rvQuests;
    private LinearLayout llCategories;
    private View notificationBadge;

    private QuestAdapter questAdapter;
    private AppDatabase database;
    private User currentUser;
    private List<Quest> allQuests;
    private String selectedCategory = "Все";
    private ExecutorService executorService;
    private Timer notificationCheckTimer;

    private final String[] categories = {"Все", "Спорт", "Финансы", "Искусство", "Обучение", "Здоровье"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);

        initViews();
        setupCategories();
        setupRecyclerView();
        setupListeners();

        loadCurrentUser();
        startNotificationCheck();
    }

    private void initViews() {
        tvNickname = findViewById(R.id.tvNickname);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        etSearch = findViewById(R.id.etSearch);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivNotifications = findViewById(R.id.ivNotifications);
        ivFilter = findViewById(R.id.ivFilter);
        btnGoToPopular = findViewById(R.id.btnGoToPopular);
        rvQuests = findViewById(R.id.rvQuests);
        llCategories = findViewById(R.id.llCategories);

        try {
            notificationBadge = findViewById(R.id.notificationBadge);
        } catch (Exception e) {
            // Не критично
        }

        try {
            btnMessagesMenu = findViewById(R.id.btnMessagesMenu);
        } catch (Exception e) {
            // Не критично
        }

        try {
            btnProfileMenu = findViewById(R.id.btnProfileMenu);
        } catch (Exception e) {
            // Не критично
        }
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

            runOnUiThread(() -> {
                if (currentUser != null) {
                    tvNickname.setText(currentUser.getNickname());
                    // ИСПРАВЛЕНО: Безопасная загрузка аватара
                    com.example.sololeveling.utils.ImageUtils.loadAvatar(
                            MainActivity.this,
                            currentUser.getAvatarPath(),
                            ivAvatar
                    );
                    updateNotificationBadge();
                } else {
                    Toast.makeText(this, "Ошибка загрузки пользователя", Toast.LENGTH_SHORT).show();
                }

                initializeData();
            });
        });
    }

    private void initializeData() {
        executorService.execute(() -> {
            initializeQuests();
            initializeLessons();

            runOnUiThread(() -> {
                loadQuests();
            });
        });
    }

    private void setupCategories() {
        for (String category : categories) {
            Button categoryButton = new Button(this);
            categoryButton.setText(category);
            categoryButton.setTextColor(getResources().getColor(R.color.dark_gray));
            categoryButton.setBackgroundResource(R.drawable.category_button_background);
            categoryButton.setAllCaps(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            categoryButton.setLayoutParams(params);
            categoryButton.setPadding(32, 16, 32, 16);

            if (category.equals("Все")) {
                categoryButton.setSelected(true);
                categoryButton.setTextColor(getResources().getColor(R.color.white));
            }

            categoryButton.setOnClickListener(v -> {
                selectCategory(category);
                updateCategoryButtons();
            });

            llCategories.addView(categoryButton);
        }
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        filterQuests();
    }

    private void updateCategoryButtons() {
        for (int i = 0; i < llCategories.getChildCount(); i++) {
            Button button = (Button) llCategories.getChildAt(i);
            String buttonText = button.getText().toString();

            if (buttonText.equals(selectedCategory)) {
                button.setSelected(true);
                button.setTextColor(getResources().getColor(R.color.white));
            } else {
                button.setSelected(false);
                button.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
    }

    private void setupRecyclerView() {
        questAdapter = new QuestAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvQuests.setLayoutManager(layoutManager);
        rvQuests.setAdapter(questAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuests();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivAvatar.setOnClickListener(v -> openMyProfile());

        ivNotifications.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                intent.putExtra("userId", currentUser.getId());
                startActivity(intent);
                updateNotificationBadge();
            }
        });

        if (btnMessagesMenu != null) {
            btnMessagesMenu.setOnClickListener(v -> {
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, ChatsListActivity.class);
                    intent.putExtra("userId", currentUser.getId());
                    startActivity(intent);
                }
            });
        }

        // ИСПРАВЛЕНО: Добавлен обработчик для кнопки профиля
        if (btnProfileMenu != null) {
            btnProfileMenu.setOnClickListener(v -> openMyProfile());
        }

        tvSeeAll.setOnClickListener(v ->
                Toast.makeText(this, "Показать все квесты", Toast.LENGTH_SHORT).show()
        );

        btnGoToPopular.setOnClickListener(v ->
                Toast.makeText(this, "Популярные квесты", Toast.LENGTH_SHORT).show()
        );

        ivFilter.setOnClickListener(v ->
                Toast.makeText(this, "Фильтры", Toast.LENGTH_SHORT).show()
        );
    }

    // ИСПРАВЛЕНО: Новый метод для открытия профиля
    private void openMyProfile() {
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("userId", currentUser.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotificationBadge() {
        if (currentUser != null) {
            executorService.execute(() -> {
                int unreadCount = database.notificationDao()
                        .getUnreadNotificationsCount(currentUser.getId());
                int unreadMessages = database.messageDao()
                        .getTotalUnreadMessages(currentUser.getId());

                int totalUnread = unreadCount + unreadMessages;

                runOnUiThread(() -> {
                    if (totalUnread > 0 && notificationBadge != null) {
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else if (notificationBadge != null) {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
            });
        }
    }

    private void startNotificationCheck() {
        notificationCheckTimer = new Timer();
        notificationCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateNotificationBadge();
            }
        }, 0, 10000);
    }

    private void initializeQuests() {
        List<Quest> existingQuests = database.questDao().getAllQuests();

        if (existingQuests.isEmpty()) {
            Quest quest1 = new Quest("Качалка", "Спорт", 123, 3.6f, "gym");
            Quest quest2 = new Quest("Бег", "Спорт", 123, 4.5f, "run");
            Quest quest3 = new Quest("Финансовая грамотность", "Финансы", 123, 4.5f, "finance");
            Quest quest4 = new Quest("Рисование", "Искусство", 123, 4.5f, "art");
            Quest quest5 = new Quest("Программирование", "Обучение", 123, 4.8f, "code");
            Quest quest6 = new Quest("Кулинария", "Здоровье", 123, 4.2f, "cook");

            database.questDao().insert(quest1);
            database.questDao().insert(quest2);
            database.questDao().insert(quest3);
            database.questDao().insert(quest4);
            database.questDao().insert(quest5);
            database.questDao().insert(quest6);
        }
    }

    private void initializeLessons() {
        List<Quest> quests = database.questDao().getAllQuests();
        if (!quests.isEmpty()) {
            int firstQuestId = quests.get(0).getId();
            int lessonCount = database.lessonDao().getTotalLessonsCount(firstQuestId);

            if (lessonCount == 0) {
                LessonInitializer.initializeLessons(database);
            }
        }
    }

    private void loadQuests() {
        executorService.execute(() -> {
            allQuests = database.questDao().getAllQuests();
            runOnUiThread(this::filterQuests);
        });
    }

    private void filterQuests() {
        if (allQuests == null) return;

        String searchText = etSearch.getText().toString().toLowerCase().trim();
        List<Quest> filteredQuests = new ArrayList<>();

        for (Quest quest : allQuests) {
            boolean matchesCategory = selectedCategory.equals("Все") ||
                    quest.getCategory().equals(selectedCategory);
            boolean matchesSearch = searchText.isEmpty() ||
                    quest.getName().toLowerCase().contains(searchText);

            if (matchesCategory && matchesSearch) {
                filteredQuests.add(quest);
            }
        }

        questAdapter.setQuests(filteredQuests);
    }

    @Override
    public void onQuestClick(Quest quest) {
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, QuestDetailActivity.class);
        intent.putExtra("questId", quest.getId());
        intent.putExtra("userId", currentUser.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Quest quest) {
        executorService.execute(() -> {
            database.questDao().update(quest);
            runOnUiThread(() -> {
                String message = quest.isFavorite() ? "Добавлено в избранное" : "Удалено из избранного";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationCheckTimer != null) {
            notificationCheckTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (notificationCheckTimer != null) {
            notificationCheckTimer.cancel();
        }
    }
}