package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity {


    private EditText editTextGoal;
    private ImageButton buttonSubmitGoal1;
    private ImageButton buttonSubmitGoal2;
    private ImageButton buttonSubmitGoal3;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        home.setAlpha(1f);

        // 바텀 바 이동 이벤트
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


        editTextGoal = findViewById(R.id.editTextGoal);
        buttonSubmitGoal1 = findViewById(R.id.buttonSubmitGoal1);
        buttonSubmitGoal2 = findViewById(R.id.buttonSubmitGoal2);
        buttonSubmitGoal3 = findViewById(R.id.buttonSubmitGoal3);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userGoal = editTextGoal.getText().toString();
                switch (view.getId()) {
                    case R.id.buttonSubmitGoal1:
                        editTextGoal.setHint("목표 1을 입력하세요");
                        break;
                    case R.id.buttonSubmitGoal2:
                        editTextGoal.setHint("목표 2를 입력하세요");
                        break;
                    case R.id.buttonSubmitGoal3:
                        editTextGoal.setHint("목표 3을 입력하세요");
                        break;
                }
                if (!userGoal.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "User Goal: " + userGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter your goal", Toast.LENGTH_SHORT).show();
                }
            }
        };

        buttonSubmitGoal1.setOnClickListener(buttonClickListener);
        buttonSubmitGoal2.setOnClickListener(buttonClickListener);
        buttonSubmitGoal3.setOnClickListener(buttonClickListener);
    }

}