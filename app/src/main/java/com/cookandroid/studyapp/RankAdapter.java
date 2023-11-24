package com.cookandroid.studyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.RankViewHolder> {

    private List<RankActivity.RankItem> rankItemList;

    public RankAdapter(List<RankActivity.RankItem> rankItemList) {
        this.rankItemList = rankItemList;
    }

    public void setData(List<RankActivity.RankItem> rankItemList) {
        this.rankItemList = rankItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_list_item, parent, false);
        return new RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {
        RankItem rankItem = rankItemList.get(position);

        // 순위는 4위부터 시작하도록 설정
        holder.rankTextView.setText((position + 4) + "위");
        holder.groupNameTextView.setText(rankItem.getGroupName());
        holder.totalStudyTimeTextView.setText(rankItem.getTotalStudyTime());

    }

    @Override
    public int getItemCount() {
        return rankItemList.size();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder {

        TextView rankTextView;
        TextView groupNameTextView;
        TextView totalStudyTimeTextView;

        public RankViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            totalStudyTimeTextView = itemView.findViewById(R.id.totalStudyTimeTextView);
        }
    }
}

