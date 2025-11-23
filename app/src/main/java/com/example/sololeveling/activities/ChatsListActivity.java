// ChatsListActivity.java
package com.example.sololeveling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.adapters.ChatsAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Message;
import com.example.sololeveling.models.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatsListActivity extends AppCompatActivity implements ChatsAdapter.OnChatClickListener {

    private ImageView ivBack;
    private RecyclerView rvChats;
    private ChatsAdapter chatsAdapter;
    private AppDatabase database;
    private ExecutorService executorService;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);

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
        loadChats();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvChats = findViewById(R.id.rvChats);
    }

    private void setupRecyclerView() {
        chatsAdapter = new ChatsAdapter(this);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(chatsAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadChats() {
        executorService.execute(() -> {
            List<Integer> partners = database.messageDao().getConversationPartners(currentUserId);
            List<ChatsAdapter.ChatItem> chatItems = new ArrayList<>();

            for (Integer partnerId : partners) {
                User partner = database.userDao().getUserById(partnerId);
                Message lastMessage = database.messageDao().getLastMessage(currentUserId, partnerId);
                int unreadCount = database.messageDao().getUnreadMessagesCount(currentUserId, partnerId);

                if (partner != null && lastMessage != null) {
                    chatItems.add(new ChatsAdapter.ChatItem(partner, lastMessage, unreadCount));
                }
            }

            runOnUiThread(() -> chatsAdapter.setChats(chatItems));
        });
    }

    @Override
    public void onChatClick(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userId", currentUserId);
        intent.putExtra("otherUserId", user.getId());
        intent.putExtra("otherUserName", user.getNickname());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}