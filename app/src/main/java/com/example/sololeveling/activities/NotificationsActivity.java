// NotificationsActivity.java
package com.example.sololeveling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.adapters.NotificationsAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Notification;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnNotificationClickListener {

    private ImageView ivBack;
    private RecyclerView rvNotifications;
    private NotificationsAdapter notificationsAdapter;
    private AppDatabase database;
    private ExecutorService executorService;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        currentUserId = getIntent().getIntExtra("userId", -1);

        if (currentUserId == -1) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvNotifications = findViewById(R.id.rvNotifications);
    }

    private void setupRecyclerView() {
        notificationsAdapter = new NotificationsAdapter(this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationsAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        executorService.execute(() -> {
            List<Notification> notifications = database.notificationDao().getAllNotifications(currentUserId);
            runOnUiThread(() -> notificationsAdapter.setNotifications(notifications));

            // Отметить все как прочитанные
            database.notificationDao().markAllAsRead(currentUserId);
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if ("message".equals(notification.getType())) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("userId", currentUserId);
            intent.putExtra("otherUserId", notification.getRelatedUserId());
            startActivity(intent);
        } else if ("comment".equals(notification.getType())) {
            Intent intent = new Intent(this, QuestDetailActivity.class);
            intent.putExtra("questId", notification.getRelatedQuestId());
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
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