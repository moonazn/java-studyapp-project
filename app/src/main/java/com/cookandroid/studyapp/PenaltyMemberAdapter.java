package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;
import static com.cookandroid.studyapp.PenaltyCalcActivity.penaltyAmount;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PenaltyMemberAdapter extends RecyclerView.Adapter<PenaltyMemberAdapter.ViewHolder> {
    private List<MemberWithPenalty> memberList;
    private List<ViewHolder> holders = new ArrayList<>(); // ViewHolder 객체를 저장할 리스트


    public PenaltyMemberAdapter(List<MemberWithPenalty> memberList) {
        this.memberList = memberList;
    }

    // 새로운 데이터로 어댑터를 업데이트하는 메서드
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<MemberWithPenalty> newMemberList) {
        memberList.clear();
        memberList.addAll(newMemberList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item2, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        holders.add(viewHolder); // ViewHolder 객체를 리스트에 추가
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberWithPenalty member = memberList.get(position);
        holder.memberName.setText(member.getMemberName());
        holder.initial.setText(member.getMemberName().substring(0, 2));
        holder.penalty.setText(String.valueOf(member.getPenaltyAmount()));

//        // Random 객체 생성
//        Random random = new Random();
//
//        // 범위 내에서 랜덤 정수 생성
//        int randomNumber = random.nextInt(4);
//
//        holder.penalty.setText(String.valueOf(member.getPenaltyAmount() - (penaltyAmount) * randomNumber));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;

        TextView initial;
        TextView penalty;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberFullname);
            initial = itemView.findViewById(R.id.memberInitials);
            penalty = itemView.findViewById(R.id.fineAmount);
        }
    }

}
