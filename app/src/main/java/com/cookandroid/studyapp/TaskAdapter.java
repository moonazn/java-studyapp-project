package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.HomeActivity.selectedDate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskItem> taskList;
    private Context context;

    public TaskAdapter(List<TaskItem> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskItem taskItem = taskList.get(position);
        holder.taskTextView.setText(taskItem.getTaskName());
        holder.checkbox.setChecked(taskItem.isCompleted());

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child("-"+uid);

        // 체크박스 상태 변경 리스너 설정
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 체크박스 상태가 변경되었을 때의 동작
                taskItem.setCompleted(isChecked);

                // Firebase Realtime Database 업데이트
                updateIsCompletedInDatabase(taskItem, usersRef);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskTextView;
        public ToggleButton checkbox;

        public ViewHolder(View view) {
            super(view);
            taskTextView = view.findViewById(R.id.task);
            checkbox = view.findViewById(R.id.checkbox);
        }
    }


    // Firebase Realtime Database에 isCompleted 값을 업데이트하는 메서드
    private void updateIsCompletedInDatabase(TaskItem taskItem, DatabaseReference usersRef) {
        DatabaseReference tasksRef = usersRef.child("date").child(selectedDate);

        // TaskItem에서 getTaskId 메서드를 활용하여 taskId 가져오기
        String taskId = taskItem.getTaskId();
        Log.d("TaskAdapter", "taskID = " + taskId);

        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // dataSnapshot이 null이 아니라면 taskId에 해당하는 데이터가 존재함
                if (dataSnapshot.exists()) {
                    // taskId가 존재하므로 isCompleted 필드 업데이트
                    tasksRef.child(taskId).child("isCompleted").setValue(taskItem.isCompleted());
                } else {
                    // taskId가 존재하지 않으면 아무 동작도 하지 않음
                    Log.d("TaskAdapter", "Task with taskId " + taskId + " does not exist in the database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                Log.e("TaskAdapter", "Error checking taskId existence: " + databaseError.getMessage());
            }
        });
    }

}
