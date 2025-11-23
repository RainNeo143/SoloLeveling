// ProfileActivity.java - Редактирование своего профиля
package com.example.sololeveling.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar, ivBack;
    private EditText etNickname, etCity, etBio;
    private Button btnChangeAvatar, btnSaveProfile, btnChangePassword;
    private AppDatabase database;
    private User currentUser;
    private ExecutorService executorService;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
        loadUserProfile();
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
            // TODO: Добавить активити для смены пароля
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

            runOnUiThread(() -> {
                if (currentUser != null) {
                    etNickname.setText(currentUser.getNickname());
                    etCity.setText(currentUser.getCity());
                    etBio.setText(currentUser.getBio());

                    if (currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) {
                        ivProfileAvatar.setImageURI(Uri.parse(currentUser.getAvatarPath()));
                    }
                }
            });
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
                finish();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            ivProfileAvatar.setImageURI(selectedImage);

            executorService.execute(() -> {
                currentUser.setAvatarPath(selectedImage.toString());
                database.userDao().update(currentUser);
                runOnUiThread(() ->
                        Toast.makeText(this, "Аватар обновлена", Toast.LENGTH_SHORT).show()
                );
            });
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
