package com.cookandroid.studyapp;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;

public class AddStopwatchActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private ImageView startButton;
    private ImageView pauseButton;
    private ImageView stopButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stopwatch);

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("00:00:00");
        startButton = findViewById(R.id.timestart);
        pauseButton = findViewById(R.id.timepause);
        stopButton = findViewById(R.id.timestop);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopwatch();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseStopwatch();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                isRunning = false;
                pauseOffset = 0;
            }
        });
    }

    private void toggleStopwatch() {
        if (isRunning) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        } else {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
        }
        isRunning = !isRunning;
    }

    private void pauseStopwatch() {
        if (isRunning) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isRunning = false;
        }
    }
}





/* 지우기 말기

import android.util.Log;
import android.view.OrientationEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


        // Firebase Realtime Database에서 할 일 데이터 가져오기
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
        databaseReference.child("user_task").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userTask = dataSnapshot.getValue(String.class);

                    // 할 일 텍스트뷰에 데이터 설정
                    if (userTask != null) {
                        taskTitleTextView.setText("할 일: " + userTask);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 처리 중 오류가 발생한 경우의 처리
                Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
                // 오류를 사용자에게 표시하거나 기타 조치를 취할 수 있음
            }
        });
        */
