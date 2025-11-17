package com.example.sololeveling.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sololeveling.R;

public class ProgressActivity extends AppCompatActivity {

    private ImageView ivBack, ivMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivMenu = findViewById(R.id.ivMenu);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Меню", Toast.LENGTH_SHORT).show();
        });
    }
}