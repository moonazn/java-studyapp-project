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

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<String> memberList;
    private boolean isEditMode = false; // 편집 모드 여부
    private List<ViewHolder> holders = new ArrayList<>(); // ViewHolder 객체를 저장할 리스트


    public MemberAdapter(List<String> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false);
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

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {

        isEditMode = editMode;
        notifyDataSetChanged(); // 변경 내용을 즉시 반영

        // RecyclerView 내의 모든 뷰 홀더에 대해 가시성 업데이트
        for (ViewHolder holder : holders) {
            if (isEditMode) {
                holder.deleteButton.setVisibility(View.VISIBLE); // 편집 모드일 때 삭제 버튼 표시
            } else {
                holder.deleteButton.setVisibility(View.GONE); // 편집 모드 아닐 때 삭제 버튼 숨김
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        LinearLayout deleteButton; // 삭제 버튼
        TextView initial;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberFullname);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            initial = itemView.findViewById(R.id.memberInitials);

            if (isEditMode) {
                deleteButton.setVisibility(View.VISIBLE); // 편집 모드일 때 삭제 버튼 표시
            } else {
                deleteButton.setVisibility(View.GONE); // 편집 모드 아닐 때 삭제 버튼 숨김
            }

            // 삭제 버튼 클릭 시 멤버 삭제 처리
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
//                        memberList.remove(position);
//                        notifyItemRemoved(position);
                        showDeleteConfirmationDialog(position, itemView);

                    }
                }
            });
        }
    }

    private void showDeleteConfirmationDialog(final int position, View itemView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setTitle("삭제 확인");
        builder.setMessage("정말로 이 멤버를 삭제하시겠습니까?");

        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 사용자가 "삭제" 버튼을 클릭했을 때 실행할 작업
                deleteMember(position);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 사용자가 "취소" 버튼을 클릭했을 때 실행할 작업 (아무것도 하지 않음)
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMember(int position) {
        String memberName = memberList.get(position);

        // 데이터베이스에서 해당 멤버 삭제
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Group/" + groupKey);
        groupRef.child("members").child(memberName).removeValue();

        // 다시 개인 그룹 멤버에 추가
        JoinInfoActivity.addMemberToDefaultGroup(memberName);

        // RecyclerView에서도 삭제
        memberList.remove(position);
        notifyItemRemoved(position);
    }

}
