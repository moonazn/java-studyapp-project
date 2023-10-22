package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HomeActivity extends AppCompatActivity {
    private ToggleButton toggleButton;
    private EditText editTextGoal;
    private EditText editTextGoal2;
    private ImageView plusButton;
    private ImageView plusButton2;
    private ImageView stopwatchButton;
    private CalendarView calendarViewMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        home.setAlpha(1f);

        home.setOnClickListener(v -> {
            if (!getClass().equals(HomeActivity.class)) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        plusButton = findViewById(R.id.plus_additional);
        editTextGoal = findViewById(R.id.et_memo);

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ListActivity.class);
                startActivity(intent);
                // 클릭 이벤트 처리 코드 추가
                String userGoal = editTextGoal.getText().toString();
                if (!userGoal.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "User Goal: " + userGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter your goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        plusButton2 = findViewById(R.id.plus_additional2);
        //editTextGoal2 = findViewById(R.id.et_memo_goal);

        plusButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
                startActivity(intent);
                // 클릭 이벤트 처리 코드 추가
                String userGoal = editTextGoal2.getText().toString();
                if (!userGoal.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "User Goal: " + userGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter your goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopwatchButton = findViewById(R.id.stopwatch);
        stopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddStopwatchActivity.class);
                startActivity(intent);
            }
        });



        toggleButton = findViewById(R.id.toggleButton);
        calendarViewMonthly = findViewById(R.id.calendarViewMonthly);
        calendarViewMonthly.setShownWeekCount(1);
    }
}
