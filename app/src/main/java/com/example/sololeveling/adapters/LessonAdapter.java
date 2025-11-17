package com.example.sololeveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sololeveling.R;
import com.example.sololeveling.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons = new ArrayList<>();
    private OnLessonClickListener listener;
    private int firstAvailableLessonIndex = 0;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson, int position);
    }

    public LessonAdapter(OnLessonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson, position);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
        calculateFirstAvailableLesson();
        notifyDataSetChanged();
    }

    private void calculateFirstAvailableLesson() {
        for (int i = 0; i < lessons.size(); i++) {
            if (!lessons.get(i).isCompleted()) {
                firstAvailableLessonIndex = i;
                break;
            }
        }
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLessonTitle, tvLessonDescription, tvExperience, tvLessonType;
        private ImageView ivLessonStatus, ivChevron;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDescription = itemView.findViewById(R.id.tvLessonDescription);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvLessonType = itemView.findViewById(R.id.tvLessonType);
            ivLessonStatus = itemView.findViewById(R.id.ivLessonStatus);
            ivChevron = itemView.findViewById(R.id.ivChevron);
        }

        public void bind(Lesson lesson, int position) {
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonDescription.setText(lesson.getDescription());
            tvExperience.setText("+" + lesson.getExperienceReward() + " XP");

            // Тип урока
            String type = "";
            switch (lesson.getType()) {
                case "daily":
                    type = "• Ежедневный";
                    break;
                case "weekly":
                    type = "• Еженедельный";
                    break;
                case "challenge":
                    type = "• Испытание";
                    break;
            }
            tvLessonType.setText(type);

            // Статус урока
            if (lesson.isCompleted()) {
                // Урок завершен
                ivLessonStatus.setImageResource(R.drawable.ic_lesson_completed);
                itemView.setAlpha(0.7f);
            } else if (position == firstAvailableLessonIndex) {
                // Урок доступен
                ivLessonStatus.setImageResource(R.drawable.ic_lesson_available);
                itemView.setAlpha(1.0f);
            } else {
                // Урок заблокирован
                ivLessonStatus.setImageResource(R.drawable.ic_lesson_locked);
                itemView.setAlpha(0.5f);
            }

            // Обработчик клика
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (lesson.isCompleted() || position == firstAvailableLessonIndex) {
                        listener.onLessonClick(lesson, position);
                    }
                }
            });
        }
    }
}