package com.example.sololeveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.models.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public static class CommentItem {
        public Comment comment;
        public String userName;
        public int repliesCount;
        public boolean isReply;

        public CommentItem(Comment comment, String userName, int repliesCount, boolean isReply) {
            this.comment = comment;
            this.userName = userName;
            this.repliesCount = repliesCount;
            this.isReply = isReply;
        }
    }

    private List<CommentItem> comments = new ArrayList<>();
    private OnCommentClickListener listener;

    public interface OnCommentClickListener {
        void onReplyClick(Comment comment);
        void onDeleteClick(Comment comment);
    }

    public CommentAdapter(OnCommentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem item = comments.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<CommentItem> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvCommentText, tvTimestamp, tvReply, tvRepliesCount, tvDelete;
        private View replyIndicator;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvRepliesCount = itemView.findViewById(R.id.tvRepliesCount);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            replyIndicator = itemView.findViewById(R.id.replyIndicator);
        }

        public void bind(CommentItem item) {
            tvUserName.setText(item.userName);
            tvCommentText.setText(item.comment.getText());


            // Форматирование времени
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(new Date(item.comment.getTimestamp())));

            // Индикатор ответа
            if (item.isReply) {
                replyIndicator.setVisibility(View.VISIBLE);
                tvReply.setVisibility(View.GONE);
                tvRepliesCount.setVisibility(View.GONE);
            } else {
                replyIndicator.setVisibility(View.GONE);
                tvReply.setVisibility(View.VISIBLE);

                if (item.repliesCount > 0) {
                    tvRepliesCount.setVisibility(View.VISIBLE);
                    tvRepliesCount.setText(item.repliesCount + " " +
                            (item.repliesCount == 1 ? "ответ" : "ответов"));
                } else {
                    tvRepliesCount.setVisibility(View.GONE);
                }
            }

            // Обработчики кликов
            tvReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClick(item.comment);
                }
            });

            tvDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item.comment);
                }
            });
        }
    }
}