package com.cookandroid.studyapp;

import static android.service.controls.ControlsProviderService.TAG;
import static com.cookandroid.studyapp.HomeActivity.selectedDate;
import static com.cookandroid.studyapp.HomeActivity.taskItemList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class TaskPopupDialog {
    private Context context;
    private Dialog popupDialog;
    private EditText etNewTask;
    private Button btnAddTask;
    private DatabaseReference databaseReference;
    private TaskAdapter taskAdapter;

    public TaskPopupDialog(Context context, DatabaseReference databaseReference, String selectedDate, TaskAdapter taskAdapter) {
        this.context = context;
        this.databaseReference = databaseReference;
        popupDialog = new Dialog(context);
        popupDialog.setContentView(R.layout.todo_popup_layout);

        etNewTask = popupDialog.findViewById(R.id.etNewTaskPopup);
        btnAddTask = popupDialog.findViewById(R.id.btnAddTaskPopup);
        this.taskAdapter = taskAdapter;
        Log.d("taskPopup", "taskAdapter = " + taskAdapter);

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTaskName = etNewTask.getText().toString().trim();
                if (!newTaskName.isEmpty()) {
                     String taskId = databaseReference.child("date").child(selectedDate).push().getKey();

                    // Create a TaskItem object
                    TaskItem newTask = new TaskItem(taskId, newTaskName, false);

                    // Convert TaskItem object to Map
                    Map<String, Object> taskMap = newTask.toMap();

                    // Add the new task to the database
                    databaseReference.child("date").child(selectedDate).child(taskId).setValue(taskMap);


                    etNewTask.setText("");

                    // RecyclerView 갱신
                    updateTaskList();

                } else {
                    Toast.makeText(context, "할 일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void show() {
        Context context = popupDialog.getContext();
        final Dialog dialog = new Dialog(context);

        popupDialog.getWindow().setBackgroundDrawableResource(R.drawable.round_rectangle);

        popupDialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    }


    // 팝업 창에서 할 일을 추가한 후 호출되는 메서드
    private void updateTaskList() {
        DatabaseReference tasksRef = databaseReference.child("date").child(selectedDate);

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskItemList.clear(); // Clear existing data

                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    String taskName = taskSnapshot.child("taskName").getValue(String.class);
                    Boolean isCompletedObject = taskSnapshot.child("isCompleted").getValue(Boolean.class);
                    boolean isCompleted = (isCompletedObject != null) ? isCompletedObject.booleanValue() : false;

                    // 새로운 데이터의 고유한 post_id를 설정
                    String task_id = taskSnapshot.getKey();

                    // Create a TaskItem object and add it to the list
                    TaskItem taskItem = new TaskItem(task_id, taskName, isCompleted);
                    taskItemList.add(taskItem);
                }

                // Notify the adapter that the data has changed
                if (taskAdapter != null) {
                    Log.d(TAG, "taskAdapter.notifyDataSetChanged in TaskPopupDialog");
                    taskAdapter.notifyDataSetChanged();

                    // 창을 닫는다
                    popupDialog.dismiss();
// 팝업 창이 열린 상태에서만 UI 갱신을 시도
                    if (popupDialog.isShowing() && taskAdapter != null) {
                        Log.d(TAG, "taskAdapter.notifyDataSetChanged in TaskPopupDialog");
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("TaskPopupDialog", "Error loading tasks for date: " + selectedDate, databaseError.toException());
            }
        });
    }
}
