package com.cookandroid.studyapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddStopwatchActivity extends AppCompatActivity implements SensorEventListener {
    private TextView stopwatchTextView;
    private Button startButton, stopButton, resetButton;
    private int seconds = 0;
    private boolean isRunning = false;
    private SharedPreferences sharedPreferences;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isFaceDown = false;

    private final BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 알람이 울릴 때마다 스톱워치 초기화
            SharedPreferences sharedPreferences = context.getSharedPreferences("StopwatchPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("stopwatchTime", 0);
            editor.apply();
        }
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stopwatch);

        stopwatchTextView = findViewById(R.id.stopwatchTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        resetButton = findViewById(R.id.resetButton);

        sharedPreferences = getSharedPreferences("StopwatchPrefs", MODE_PRIVATE);

        // 알람을 받기 위한 BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter("your_alarm_action");
        registerReceiver(alarmReceiver, filter);

        // 센서 매니저 및 리스너 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 버튼 리스너 설정
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopwatch();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStopwatch();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStopwatch();
            }
        });

        // SharedPreferences에서 스톱워치 시간 로드
        int initialTime = sharedPreferences.getInt("stopwatchTime", 0);
        seconds = initialTime;
        updateStopwatchText();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 이벤트 처리
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            // 여기에서 가속도 값(values)을 이용해 화면이 위아래로 뒤집히는지 감지합니다.
            // 원하는 방향의 뒤집힘을 감지하면 스톱워치를 시작하거나 멈추도록 제어할 수 있습니다.

            // 예를 들어, 화면이 아래로 뒤집힌 상태를 감지하려면 다음과 같이 할 수 있습니다.
            if (values[2] < -8 && !isFaceDown) {
                isFaceDown = true;
                startStopwatch(); // 스톱워치 시작
            } else if (values[2] > 8 && isFaceDown) {
                isFaceDown = false;
                stopStopwatch(); // 스톱워치 멈춤
            }
        }
    }

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                seconds++;
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                stopwatchTextView.setText(time);

                // 현재 시간을 SharedPreferences에 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("stopwatchTime", seconds);
                editor.apply();

                handler.postDelayed(this, 1000);
            }
        }
    };

    private void startStopwatch() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        resetButton.setEnabled(true);
        handler.post(runnable);
    }

    private void stopStopwatch() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        // 토스트 메시지를 띄웁니다.
        showToast("스톱워치가 멈췄습니다.");
    }

    private void resetStopwatch() {
        isRunning = false;
        seconds = 0;
        updateStopwatchText();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        // SharedPreferences에서 시간 초기화
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stopwatchTime", 0);
        editor.apply();
    }

    private void updateStopwatchText() {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
        stopwatchTextView.setText(time);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티 종료 시 BroadcastReceiver 해제
        unregisterReceiver(alarmReceiver);
        // 센서 리스너 등록 해제
        sensorManager.unregisterListener(this);
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