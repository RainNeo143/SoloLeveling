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

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbRememberPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etLogin = findViewById(R.id.etLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbRememberPassword = findViewById(R.id.cbRememberPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String login = etLogin.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация
        if (login.isEmpty()) {
            etLogin.setError("Введите логин");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Введите email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Неверный формат email");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return;
        }

        // ИСПРАВЛЕНО: Регистрация теперь в фоновом потоке
        executorService.execute(() -> {
            // Проверка существования пользователя
            User existingUser = database.userDao().getUserByLogin(login);
            if (existingUser != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Создание пользователя
            User newUser = new User(login, email, password, login);
            database.userDao().insert(newUser);

            // Получить созданного пользователя с ID
            User createdUser = database.userDao().getUserByLogin(login);

            runOnUiThread(() -> {
                // Сохранение в SharedPreferences если нужно запомнить
                if (cbRememberPassword.isChecked()) {
                    SharedPreferences prefs = getSharedPreferences("SoloLevelingPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("login", login)
                            .putString("password", password)
                            .putBoolean("remember", true)
                            .apply();
                }

                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

                // Переход на главный экран
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.putExtra("userId", createdUser.getId());
                startActivity(intent);
                finish();
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