package com.example.sololeveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.models.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private List<Quest> quests = new ArrayList<>();
    private OnQuestClickListener listener;

    public interface OnQuestClickListener {
        void onQuestClick(Quest quest);
        void onFavoriteClick(Quest quest);
    }

    public QuestAdapter(OnQuestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = quests.get(position);
        holder.bind(quest);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void setQuests(List<Quest> quests) {
        this.quests = quests;
        notifyDataSetChanged();
    }

    class QuestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuestName, tvPrice, tvRating;
        private ImageView ivQuestIcon, ivFavorite;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestName = itemView.findViewById(R.id.tvQuestName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivQuestIcon = itemView.findViewById(R.id.ivQuestIcon);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }

        public void bind(Quest quest) {
            tvQuestName.setText(quest.getName());
            tvPrice.setText("$ " + quest.getPrice());
            tvRating.setText(String.valueOf(quest.getRating()));

            // Установка иконки в зависимости от типа
            int iconResource = getIconResource(quest.getIconType());
            ivQuestIcon.setImageResource(iconResource);

            // Установка иконки избранного
            if (quest.isFavorite()) {
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            }

            // Обработчики кликов
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestClick(quest);
                }
            });

            ivFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    quest.setFavorite(!quest.isFavorite());
                    notifyItemChanged(getAdapterPosition());
                    listener.onFavoriteClick(quest);
                }
            });
        }

        private int getIconResource(String iconType) {
            switch (iconType) {
                case "gym":
                    return R.drawable.ic_gym;
                case "run":
                    return R.drawable.ic_run;
                case "finance":
                    return R.drawable.ic_finance;
                case "art":
                    return R.drawable.ic_art;
                case "code":
                    return R.drawable.ic_code;
                case "cook":
                    return R.drawable.ic_cook;
                default:
                    return R.drawable.ic_quest_default;
            }
        }
    }
}