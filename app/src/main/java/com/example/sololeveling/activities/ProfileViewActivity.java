// ProfileViewActivity.java - Просмотр профиля другого пользователя
package com.example.sololeveling.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar, ivBack;
    private TextView tvNickname, tvLevel, tvCity, tvBio, tvMemberSince;
    private Button btnSendMessage;
    private AppDatabase database;
    private User viewedUser;
    private User currentUser;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
        loadProfiles();
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        ivBack = findViewById(R.id.ivBack);
        tvNickname = findViewById(R.id.tvNickname);
        tvLevel = findViewById(R.id.tvLevel);
        tvCity = findViewById(R.id.tvCity);
        tvBio = findViewById(R.id.tvBio);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        btnSendMessage = findViewById(R.id.btnSendMessage);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSendMessage.setOnClickListener(v -> {
            if (viewedUser != null && currentUser != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("otherUserId", viewedUser.getId());
                intent.putExtra("otherUserName", viewedUser.getNickname());
                intent.putExtra("userId", currentUser.getId());
                startActivity(intent);
            }
        });
    }

    private void loadProfiles() {
        int viewedUserId = getIntent().getIntExtra("userId", -1);
        int currentUserId = getIntent().getIntExtra("currentUserId", -1);

        if (viewedUserId == -1 || currentUserId == -1) {
            finish();
            return;
        }

        executorService.execute(() -> {
            viewedUser = database.userDao().getUserById(viewedUserId);
            currentUser = database.userDao().getUserById(currentUserId);

            runOnUiThread(() -> {
                if (viewedUser != null) {
                    updateUI();

                    if (viewedUserId == currentUserId) {
                        btnSendMessage.setText("РЕДАКТИРОВАТЬ ПРОФИЛЬ");
                        btnSendMessage.setOnClickListener(v -> {
                            Intent intent = new Intent(this, ProfileActivity.class);
                            intent.putExtra("userId", currentUserId);
                            startActivity(intent);
                        });
                    }
                }
            });
        });
    }

    private void updateUI() {
        tvNickname.setText(viewedUser.getNickname());
        tvLevel.setText("Уровень: " + viewedUser.getLevel());
        tvCity.setText(viewedUser.getCity().isEmpty() ? "Город не указан" : viewedUser.getCity());
        tvBio.setText(viewedUser.getBio().isEmpty() ? "О себе не рассказал" : viewedUser.getBio());

        if (viewedUser.getAvatarPath() != null && !viewedUser.getAvatarPath().isEmpty()) {
            ivProfileAvatar.setImageURI(Uri.parse(viewedUser.getAvatarPath()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String joinDate = sdf.format(new Date(viewedUser.getCreatedDate()));
        tvMemberSince.setText("Участник с " + joinDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}