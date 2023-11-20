package com.cookandroid.studyapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalEditPopupDialog extends Dialog {

    private Context context;

    private GoalItem goalItem;
    private String targetDateStr;
    private int targetHour;
    private int targetMinute;
    private GoalAdapter goalAdapter;
    private int position;

    public GoalEditPopupDialog(@NonNull Context context, GoalItem goalItem, GoalAdapter goalAdapter, int position) {
        super(context);
        this.context = context;
        this.goalItem = goalItem;
        this.goalAdapter = goalAdapter;
        this.position = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goal_edit_popup_layout);

        // 디데이와 남은 시간을 계산하여 TextView에 설정
        TextView ddayTextView = findViewById(R.id.dday);
        TextView timerTextView = findViewById(R.id.timer);

        String dday = goalItem.getDday();
        ddayTextView.setText("D - " + dday);

        targetDateStr = goalItem.getTarget_date();
        targetHour = goalItem.getTarget_hour();
        targetMinute = goalItem.getTarget_minute();
        long remainingTimeMillis = calculateRemainingTime();
        startCountDownTimer(remainingTimeMillis, timerTextView);

        // 확인 버튼 클릭 시 팝업 닫기
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // GoalAdapter의 deleteGoal 메서드를 호출하여 목표 삭제
                if (goalAdapter != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("삭제 확인");
                    builder.setMessage("해당 목표를 정말 삭제하시겠습니까?");

                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goalAdapter.deleteGoal(position);
                            dismiss();
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

                // 팝업 닫기
                dismiss();
            }
        });

    }
    private long calculateRemainingTime() {
        // 남은 시간 계산
        Calendar targetCalendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());

        try {
            targetCalendar.setTime(dateFormat.parse(targetDateStr));
            targetCalendar.set(Calendar.HOUR_OF_DAY, targetHour);
            targetCalendar.set(Calendar.MINUTE, targetMinute);
            targetCalendar.set(Calendar.SECOND, 0);

            Calendar currentCalendar = Calendar.getInstance();

            return targetCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void startCountDownTimer(long remainingTimeMillis, final TextView timerTextView) {
        // 남은 시간을 표시하는 CountDownTimer 시작
        new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;

                // 시간, 분, 초를 TextView에 설정
                String remainingTimeText = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        hours % 24, minutes % 60, seconds % 60);

                timerTextView.setText(remainingTimeText);
            }

            @Override
            public void onFinish() {
                // 타이머가 종료될 때의 동작
            }
        }.start();
    }

    public static void deleteGoalFromDatabase(GoalItem goalItem) {
        String goalId = goalItem.getGoalId(); // 목표 아이템의 고유 ID 가져오기
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 목표 아이템을 삭제할 데이터베이스 참조 설정
        DatabaseReference goalsRef = FirebaseDatabase.getInstance().getReference("users")
                .child("-" + uid)
                .child("goals")
                .child(goalId);

        // 목표 아이템을 데이터베이스에서 삭제
        goalsRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // 삭제가 성공적으로 이루어졌을 때 수행할 작업
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 삭제가 실패한 경우 수행할 작업
                    }
                });
    }

}
