package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.GoalEditPopupDialog.deleteGoalFromDatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private List<GoalItem> goalList;
    private Context context;
    private GoalClickListener goalClickListener;

    public interface GoalClickListener {
        void onGoalClicked(int position);
    }
    // GoalClickListener를 설정하는 메서드 추가
    public void setGoalClickListener(GoalClickListener listener) {
        this.goalClickListener = listener;
    }

    public GoalAdapter(Context context, List<GoalItem> goalList) {
        this.context = context;
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoalItem goalItem = goalList.get(position);
        holder.textViewGoal.setText(goalItem.getGoalName());
        holder.textViewDday.setText("D-" + goalItem.getDday());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 이벤트 처리
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && goalClickListener != null) {
                    goalClickListener.onGoalClicked(adapterPosition);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoal;
        TextView textViewDday;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoal = itemView.findViewById(R.id.textViewGoal);
            textViewDday = itemView.findViewById(R.id.textViewDday);
        }
    }
    // 삭제 메서드 추가
    public void deleteGoal(int position) {
        if (position != RecyclerView.NO_POSITION) {
            GoalItem deletedGoal = goalList.get(position);

            // 데이터베이스에서 목표 삭제
            deleteGoalFromDatabase(deletedGoal);

            // 어댑터에서 목표 아이템 삭제하고 갱신
            goalList.remove(position);
            notifyItemRemoved(position);
        }
    }

}
