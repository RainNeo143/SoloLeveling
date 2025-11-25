package com.example.sololeveling.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.adapters.CommentAdapter;
import com.example.sololeveling.database.AppDatabase;
import com.example.sololeveling.models.Comment;
import com.example.sololeveling.models.QuestRating;
import com.example.sololeveling.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentsActivity extends AppCompatActivity implements CommentAdapter.OnCommentClickListener {

    private ImageView ivBack;
    private TextView tvQuestName, tvAverageRating, tvRatingsCount, tvYourRating;
    private RatingBar ratingBarAverage, ratingBarUser;
    private RecyclerView rvComments;
    private EditText etComment;
    private Button btnSendComment;
    private LinearLayout llReplyingTo;
    private TextView tvReplyingToName;
    private ImageView ivCancelReply;

    private CommentAdapter commentAdapter;
    private AppDatabase database;
    private ExecutorService executorService;

    private int questId;
    private int userId;
    private String questName;
    private Comment replyingToComment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        questId = getIntent().getIntExtra("questId", -1);
        userId = getIntent().getIntExtra("userId", -1);
        questName = getIntent().getStringExtra("questName");

        if (questId == -1 || userId == -1) {
            Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvQuestName = findViewById(R.id.tvQuestName);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvRatingsCount = findViewById(R.id.tvRatingsCount);
        tvYourRating = findViewById(R.id.tvYourRating);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);
        ratingBarUser = findViewById(R.id.ratingBarUser);
        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        llReplyingTo = findViewById(R.id.llReplyingTo);
        tvReplyingToName = findViewById(R.id.tvReplyingToName);
        ivCancelReply = findViewById(R.id.ivCancelReply);

        tvQuestName.setText(questName);

        commentAdapter = new CommentAdapter(this);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSendComment.setOnClickListener(v -> sendComment());

        ivCancelReply.setOnClickListener(v -> cancelReply());

        ratingBarUser.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && rating > 0) {
                saveUserRating((int) rating);
            }
        });
    }

    private void loadData() {
        executorService.execute(() -> {
            Float avgRating = database.questRatingDao().getAverageRating(questId);
            int ratingsCount = database.questRatingDao().getRatingsCount(questId);
            QuestRating userRating = database.questRatingDao().getUserRating(userId, questId);

            runOnUiThread(() -> {
                if (avgRating != null && avgRating > 0) {
                    ratingBarAverage.setRating(avgRating);
                    tvAverageRating.setText(String.format("%.1f", avgRating));
                    tvRatingsCount.setText("(" + ratingsCount + " оценок)");
                } else {
                    tvAverageRating.setText("—");
                    tvRatingsCount.setText("(нет оценок)");
                }

                if (userRating != null) {
                    ratingBarUser.setRating(userRating.getRating());
                    tvYourRating.setText("Ваша оценка: " + userRating.getRating());
                }
            });

            loadComments();
        });
    }

    private void loadComments() {
        executorService.execute(() -> {
            List<Comment> mainComments = database.commentDao().getMainComments(questId);
            List<CommentAdapter.CommentItem> items = new ArrayList<>();

            for (Comment comment : mainComments) {
                User user = database.userDao().getUserById(comment.getUserId());
                String userName = user != null ? user.getNickname() : "Пользователь";
                int repliesCount = database.commentDao().getReplies(comment.getId()).size();

                // ИСПРАВЛЕНО: Теперь передаем userId правильно
                items.add(new CommentAdapter.CommentItem(comment, userName, repliesCount, false, userId));

                List<Comment> replies = database.commentDao().getReplies(comment.getId());
                for (Comment reply : replies) {
                    User replyUser = database.userDao().getUserById(reply.getUserId());
                    String replyUserName = replyUser != null ? replyUser.getNickname() : "Пользователь";
                    // ИСПРАВЛЕНО: И здесь тоже
                    items.add(new CommentAdapter.CommentItem(reply, replyUserName, 0, true, userId));
                }
            }

            runOnUiThread(() -> commentAdapter.setComments(items));
        });
    }

    private void sendComment() {
        String text = etComment.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите текст комментария", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Integer parentId = replyingToComment != null ? replyingToComment.getId() : null;
            Comment comment = new Comment(userId, questId, parentId, text, System.currentTimeMillis());
            database.commentDao().insert(comment);

            runOnUiThread(() -> {
                etComment.setText("");
                cancelReply();
                Toast.makeText(this, "Комментарий добавлен", Toast.LENGTH_SHORT).show();
                loadComments();
            });
        });
    }

    private void saveUserRating(int rating) {
        executorService.execute(() -> {
            QuestRating questRating = new QuestRating(userId, questId, rating, System.currentTimeMillis());
            database.questRatingDao().insert(questRating);

            runOnUiThread(() -> {
                tvYourRating.setText("Ваша оценка: " + rating);
                Toast.makeText(this, "Оценка сохранена", Toast.LENGTH_SHORT).show();
                loadData();
            });
        });
    }

    @Override
    public void onReplyClick(Comment comment) {
        replyingToComment = comment;
        executorService.execute(() -> {
            User user = database.userDao().getUserById(comment.getUserId());
            runOnUiThread(() -> {
                String userName = user != null ? user.getNickname() : "Пользователь";
                tvReplyingToName.setText("Ответ для " + userName);
                llReplyingTo.setVisibility(View.VISIBLE);
                etComment.requestFocus();
            });
        });
    }

    @Override
    public void onDeleteClick(Comment comment) {
        executorService.execute(() -> {
            if (comment.getUserId() == userId) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Удалить комментарий?")
                            .setMessage("Вы уверены, что хотите удалить этот комментарий?")
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                executorService.execute(() -> {
                                    database.commentDao().delete(comment);
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Комментарий удален", Toast.LENGTH_SHORT).show();
                                        loadComments();
                                    });
                                });
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Вы можете удалять только свои комментарии", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void cancelReply() {
        replyingToComment = null;
        llReplyingTo.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}