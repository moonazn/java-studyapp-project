package com.cookandroid.studyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StopwatchActivity extends AppCompatActivity implements SensorEventListener {
    private TextView stopwatchTextView;
    private Button startButton, stopButton, resetButton, finishButton;
    private int seconds = 0;
    private boolean isRunning = false;
    private SharedPreferences sharedPreferences;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isFaceDown = false;

    private Spinner taskTitleSpinner;
    private ArrayAdapter<String> taskTitleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        stopwatchTextView = findViewById(R.id.stopwatchTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        resetButton = findViewById(R.id.resetButton);
        finishButton = findViewById(R.id.finishButton);

        sharedPreferences = getSharedPreferences("StopwatchPrefs", MODE_PRIVATE);

        // 시간 읽기
        int initialTime = sharedPreferences.getInt("stopwatchTime", 0);
        seconds = initialTime;
        updateStopwatchText();

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

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish 버튼 클릭 시 홈으로 이동
                navigateToHome();
            }
        });

        // 센서 매니저 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 센서 리스너 등록
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        taskTitleSpinner = findViewById(R.id.taskTitleSpinner);
        // Spinner에 할 일 데이터를 설정하는 어댑터 생성
        taskTitleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        taskTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTitleSpinner.setAdapter(taskTitleAdapter);

        // Firebase Realtime Database에서 할 일 데이터 가져오기
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
        DatabaseReference userTaskReference = databaseReference.child("user_task");
        userTaskReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Firebase에서 가져온 할 일 데이터를 Spinner 어댑터에 추가
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String task = taskSnapshot.getValue(String.class);
                        if (task != null) {
                            taskTitleAdapter.add(task);
                        }
                    }

                    // 어댑터 변경을 알림
                    taskTitleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 처리 중 오류가 발생한 경우의 처리
                Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
                // 오류를 사용자에게 표시하거나 기타 조치를 취할 수 있음
            }
        });
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
        finishButton.setEnabled(true);
        handler.post(runnable);
    }

    private void stopStopwatch() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);
    }

    private void resetStopwatch() {
        isRunning = false;
        seconds = 0;
        updateStopwatchText();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 센서 리스너 등록 해제
        sensorManager.unregisterListener(this);
    }

    // 홈으로 이동하는 메서드
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();  // 현재 액티비티 종료
    }
}