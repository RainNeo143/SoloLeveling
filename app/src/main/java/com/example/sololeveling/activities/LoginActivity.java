
package com.example.sololeveling.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.MainActivity;
import com.example.sololeveling.R;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginUsername, etLoginPassword;
    private CheckBox cbLoginRememberPassword;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        checkSavedCredentials();
        setupListeners();
    }

    private void initViews() {
        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        cbLoginRememberPassword = findViewById(R.id.cbLoginRememberPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void checkSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences("SoloLevelingPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember", false);

        if (remember) {
            String login = prefs.getString("login", "");
            String password = prefs.getString("password", "");

            etLoginUsername.setText(login);
            etLoginPassword.setText(password);
            cbLoginRememberPassword.setChecked(true);
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Функция восстановления пароля", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String login = etLoginUsername.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Валидация
        if (login.isEmpty()) {
            etLoginUsername.setError("Введите логин");
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Введите пароль");
            return;
        }

        // ИСПРАВЛЕНО: Вход теперь в фоновом потоке
        executorService.execute(() -> {
            // Проверка учетных данных
            User user = database.userDao().login(login, password);

            runOnUiThread(() -> {
                if (user != null) {
                    // Сохранение в SharedPreferences если нужно запомнить
                    SharedPreferences prefs = getSharedPreferences("SoloLevelingPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    if (cbLoginRememberPassword.isChecked()) {
                        editor.putString("login", login);
                        editor.putString("password", password);
                        editor.putBoolean("remember", true);
                    } else {
                        editor.clear();
                    }
                    editor.apply();

                    Toast.makeText(this, "Добро пожаловать, " + user.getNickname() + "!", Toast.LENGTH_SHORT).show();

                    // Переход на главный экран
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userId", user.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}