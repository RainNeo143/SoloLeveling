package com.example.sololeveling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.activities.ProgressActivity;
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

public class MainActivity extends AppCompatActivity implements QuestAdapter.OnQuestClickListener {

    private TextView tvNickname, tvSeeAll;
    private EditText etSearch;
    private ImageView ivAvatar, ivNotifications, ivFilter;
    private Button btnGoToPopular;
    private RecyclerView rvQuests;
    private LinearLayout llCategories;

    private QuestAdapter questAdapter;
    private AppDatabase database;
    private User currentUser;
    private List<Quest> allQuests;
    private String selectedCategory = "Все";
    private ExecutorService executorService;

    private final String[] categories = {"Все", "Спорт", "Финансы", "Искусство", "Обучение", "Здоровье"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);

        initViews();
        loadCurrentUser();
        setupCategories();
        setupRecyclerView();
        setupListeners();

        // Инициализация квестов и уроков в фоновом потоке
        executorService.execute(() -> {
            initializeQuests();
            initializeLessons();

            runOnUiThread(() -> {
                loadQuests();
            });
        });
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
    }

    private void loadCurrentUser() {
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
            tvNickname.setText(currentUser.getNickname());
        }
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
        // Поиск квестов
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

        // Переход к экрану прогресса
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
            startActivity(intent);
        });

        // Остальные обработчики
        tvSeeAll.setOnClickListener(v ->
                Toast.makeText(this, "Показать все квесты", Toast.LENGTH_SHORT).show()
        );

        btnGoToPopular.setOnClickListener(v ->
                Toast.makeText(this, "Популярные квесты", Toast.LENGTH_SHORT).show()
        );

        ivNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Уведомления", Toast.LENGTH_SHORT).show()
        );

        ivFilter.setOnClickListener(v ->
                Toast.makeText(this, "Фильтры", Toast.LENGTH_SHORT).show()
        );
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
        // Проверить, инициализированы ли уроки
        List<Quest> quests = database.questDao().getAllQuests();
        if (!quests.isEmpty()) {
            int firstQuestId = quests.get(0).getId();
            int lessonCount = database.lessonDao().getTotalLessonsCount(firstQuestId);

            // Если уроков нет, инициализировать
            if (lessonCount == 0) {
                LessonInitializer.initializeLessons(database);
            }
        }
    }

    private void loadQuests() {
        allQuests = database.questDao().getAllQuests();
        filterQuests();
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
        database.questDao().update(quest);
        String message = quest.isFavorite() ? "Добавлено в избранное" : "Удалено из избранного";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
