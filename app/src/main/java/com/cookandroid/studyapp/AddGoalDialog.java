package com.cookandroid.studyapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddGoalDialog extends Dialog {

    private Context context;
    TimePicker timePicker;
    String selectedDate;
    TextView date;

    private GoalUploadListener goalUploadListener;


    public AddGoalDialog(@NonNull Context context, GoalUploadListener listener) {
        super(context);
        this.context = context;
        this.goalUploadListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goal_popup_layout);

        // 다이얼로그 내의 뷰들을 참조
        EditText etGoalMemo = findViewById(R.id.etGoalMemo);
        timePicker = findViewById(R.id.timePicker);
        Button addButton = findViewById(R.id.addButton);
        TextView message = findViewById(R.id.message);

        date = findViewById(R.id.selectedDate);
        ImageView calendarImg = findViewById(R.id.calendarImg);

        // 현재 날짜로 초기화된 캘린더 객체 생성
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        selectedDate = year + "년 " + month + "월 " + day + "일";
        date.setText(selectedDate);

        calendarImg.setAlpha(0.5f);
        timePicker.setCurrentHour(12);
        timePicker.setCurrentMinute(00);

        calendarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        // 버튼 클릭 이벤트 처리
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 목표와 선택한 날짜, 시간을 가져와서 사용
                String goalMemo = etGoalMemo.getText().toString();

                // EditText가 비어 있는지 확인
                if (goalMemo.isEmpty()) {
                    // EditText가 비어 있으면 사용자에게 알림
                    message.setText("목표를 입력하세요.");
                } else {
                    // 시간은 TimePicker에서 가져오기 (API 23부터 getHour(), getMinute() 사용)
                    int hour, minute;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        hour = timePicker.getHour();
                        minute = timePicker.getMinute();
                    } else {
                        hour = timePicker.getCurrentHour();
                        minute = timePicker.getCurrentMinute();
                    }

                    // Toast 메시지 출력
                    String message = "목표: " + goalMemo + "\n날짜: " + selectedDate + "\n시간: " + hour + ":" + minute;
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    // Firebase Realtime Database에 데이터 업로드
                    uploadGoalToDatabase(goalMemo, selectedDate, hour, minute);

                    // 다이얼로그를 종료
                    dismiss();
                    // 목표 업로드 완료 시 리스너 호출
                    if (goalUploadListener != null) {
                        goalUploadListener.onGoalUploaded();
                    }
                }
            }
        });
    }
    private void showDatePickerDialog() {
        // 현재 날짜로 초기화된 캘린더 객체 생성
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                R.style.DatePickerTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        selectedDate = selectedYear + "년 " + (selectedMonth + 1) + "월 " + selectedDay + "일";
                        date.setText(selectedDate);
                    }
                },
                year,
                month,
                day
        );

        // 다이얼로그 표시
        datePickerDialog.show();
    }

    // Firebase Realtime Database에 데이터 업로드
    private void uploadGoalToDatabase(String goalMemo, String selectedDate, int hour, int minute) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);

        // Firebase Realtime Database에 데이터를 업로드하는 코드 작성
        // 예를 들어, "goals"라는 키 아래에 데이터를 저장한다고 가정
        DatabaseReference goalsRef = usersRef.child("goals");

        // 새로운 고유한 키를 생성하여 목표 데이터 저장
        String goalId = goalsRef.push().getKey();
        if (goalId != null) {
            goalsRef.child(goalId).child("goalName").setValue(goalMemo);
            goalsRef.child(goalId).child("target_date").setValue(selectedDate);
            goalsRef.child(goalId).child("hour").setValue(hour);
            goalsRef.child(goalId).child("minute").setValue(minute);
        }

        // 콜백 호출
        if (goalUploadListener != null) {
            goalUploadListener.onGoalUploaded();
        }
    }
    public interface GoalUploadListener {
        void onGoalUploaded();
    }
    public void setGoalUploadListener(GoalUploadListener listener) {
        this.goalUploadListener = listener;
    }
}
