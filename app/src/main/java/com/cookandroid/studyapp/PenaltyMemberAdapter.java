package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;

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

public class PenaltyMemberAdapter extends RecyclerView.Adapter<PenaltyMemberAdapter.ViewHolder> {
    private List<String> memberList;
    private List<ViewHolder> holders = new ArrayList<>(); // ViewHolder 객체를 저장할 리스트


    public PenaltyMemberAdapter(List<String> memberList) {
        this.memberList = memberList;
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
        String member = memberList.get(position);
        holder.memberName.setText(member);
        holder.initial.setText(member.substring(0, 2));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;

        TextView initial;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberFullname);
            initial = itemView.findViewById(R.id.memberInitials);

        }
    }

}
