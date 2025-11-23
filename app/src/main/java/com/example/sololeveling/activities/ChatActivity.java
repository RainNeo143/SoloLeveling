package com.example.sololeveling.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.adapters.MessageAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Message;
import com.example.sololeveling.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvOtherUserName;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSendMessage;

    private MessageAdapter messageAdapter;
    private AppDatabase database;
    private ExecutorService executorService;

    private int currentUserId;
    private int otherUserId;
    private String otherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        currentUserId = getIntent().getIntExtra("userId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (currentUserId == -1 || otherUserId == -1) {
            Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        loadMessages();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvOtherUserName = findViewById(R.id.tvOtherUserName);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        tvOtherUserName.setText(otherUserName);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        executorService.execute(() -> {
            List<Message> messages = database.messageDao().getConversation(currentUserId, otherUserId);

            runOnUiThread(() -> {
                messageAdapter.setMessages(messages);
                rvMessages.scrollToPosition(Math.max(0, messageAdapter.getItemCount() - 1));
            });

            // Отметить сообщения как прочитанные
            database.messageDao().markConversationAsRead(currentUserId, otherUserId);

            // Обновить количество непрочитанных сообщений
            executorService.execute(() -> {
                int unreadCount = database.messageDao().getTotalUnreadMessages(currentUserId);
                database.userDao().getUserById(currentUserId).setUnreadMessagesCount(unreadCount);
            });
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Message message = new Message(currentUserId, otherUserId, text, System.currentTimeMillis());
            database.messageDao().insert(message);

            // Создать уведомление для получателя
            Notification notification = new Notification(
                    otherUserId,
                    "Новое сообщение",
                    "Новое сообщение от пользователя",
                    "message",
                    currentUserId,
                    -1,
                    System.currentTimeMillis()
            );
            database.notificationDao().insert(notification);

            // Обновить счетчик непрочитанных сообщений
            int unreadCount = database.messageDao().getTotalUnreadMessages(otherUserId);
            database.userDao().getUserById(otherUserId).setUnreadMessagesCount(unreadCount);

            runOnUiThread(() -> {
                etMessage.setText("");
                loadMessages();
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