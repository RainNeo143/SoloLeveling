// ChatsAdapter.java
package com.example.sololeveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.models.Message;
import com.example.sololeveling.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    public static class ChatItem {
        public User user;
        public Message lastMessage;
        public int unreadCount;

        public ChatItem(User user, Message lastMessage, int unreadCount) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }

    private List<ChatItem> chats = new ArrayList<>();
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(User user);
    }

    public ChatsAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void setChats(List<ChatItem> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername, tvLastMessage, tvTimestamp, tvUnreadBadge;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
        }

        public void bind(ChatItem chat) {
            tvUsername.setText(chat.user.getNickname());
            tvLastMessage.setText(chat.lastMessage.getText());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(new Date(chat.lastMessage.getTimestamp())));

            if (chat.unreadCount > 0) {
                tvUnreadBadge.setText(String.valueOf(chat.unreadCount));
                tvUnreadBadge.setVisibility(View.VISIBLE);
            } else {
                tvUnreadBadge.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(chat.user);
                }
            });
        }
    }
}