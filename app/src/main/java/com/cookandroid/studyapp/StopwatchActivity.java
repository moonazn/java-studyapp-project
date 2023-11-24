package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.HomeActivity.selectedDate;
import static com.cookandroid.studyapp.MyPageActivity.groupKey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class StopwatchActivity extends AppCompatActivity implements SensorEventListener {

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();
    private TextView stopwatchTextView, message;
    private Button startButton, stopButton, resetButton;
    private ImageButton finishButton;
    private int seconds = 0;
    private boolean isRunning = false;
    private SharedPreferences sharedPreferences;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isFaceDown = false;

    private Spinner taskTitleSpinner;
    private ArrayAdapter<String> taskTitleAdapter;
    private TextView currentTaskName;
    private TextView currentTaskTime;

    // 현재 측정 중인 할 일의 총 지속 시간 (초 단위)
    private int currentTaskTotalDuration = 0;

    // 측정 시작 시간 (측정 중일 때만 업데이트)
    private long measurementStartTime = 0;
    private TextView todayTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        message = findViewById(R.id.message);
        stopwatchTextView = findViewById(R.id.stopwatchTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        resetButton = findViewById(R.id.resetButton);
        finishButton = findViewById(R.id.finishButton);

        currentTaskName = findViewById(R.id.currentTaskName);
        currentTaskTime = findViewById(R.id.currentTaskTime);
        todayTime = findViewById(R.id.todayTime);

        if (stopwatchTextView.getText() != "00:00:00") {
            stopwatchTextView.setText("00:00:00");
            Log.d("처음세팅", "stopwatchTextView 00");
        }

        // 오늘 날짜로 초기화
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더함
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        selectedDate = year + "-" + month + "-" + dayOfMonth;

        sharedPreferences = getSharedPreferences("StopwatchPrefs", MODE_PRIVATE);

        // 시간 초기화
        int initialTime = sharedPreferences.getInt("stopwatchTime", 0);
        initialTime = 0;
        seconds = initialTime;
        Log.d("시간 초기화", String.valueOf(initialTime));

        updateTodayTime();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (taskTitleSpinner.getSelectedItem() == null){
                    message.setText("측정 시작 전 할 일을 추가하고 선택하세요.");
                } else {
                    startStopwatch();
                }
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

        Button naverDictionaryButton = findViewById(R.id.naverdic);
        Button googleDictionaryButton = findViewById(R.id.googledic);

        // 네이버 사전 버튼 클릭 이벤트 처리
        naverDictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://dict.naver.com/");
            }
        });

        // 구글 사전 버튼 클릭 이벤트 처리
        googleDictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://translate.google.com/");
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveElapsedTimeToDatabase();

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
        taskTitleSpinner.clearFocus();

        // Spinner에 할 일 데이터를 설정하는 어댑터 생성
        taskTitleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        taskTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTitleSpinner.setAdapter(taskTitleAdapter);

        // Firebase Realtime Database에서 할 일 데이터 가져오기
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);
        DatabaseReference userTaskReference = databaseReference.child("date").child(selectedDate);
        userTaskReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Firebase에서 가져온 할 일 데이터를 Spinner 어댑터에 추가
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String taskName = taskSnapshot.child("taskName").getValue(String.class);
                        String taskId = taskSnapshot.child("taskId").getValue(String.class);
                        if (taskName != null) {
                            taskTitleAdapter.add(taskName);
                            Log.d("FirebaseData", "Task Name: " + taskName);
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

        if (taskTitleSpinner.getSelectedItem() == null){
            message.setText("측정 시작 전 할 일을 추가하고 선택하세요.");
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
        }

        taskTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                message.setText("");
                startButton.setEnabled(true);
                stopButton.setEnabled(true);
                // 현재 선택된 할 일의 이름 가져오기
                String selectedTask = taskTitleSpinner.getSelectedItem().toString();
                currentTaskName.setText(selectedTask);

                // Firebase Realtime Database에 데이터 저장
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);
                DatabaseReference userTaskReference = databaseReference.child("date").child(selectedDate);

                Query query = userTaskReference.orderByChild("taskName").equalTo(selectedTask);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // duration 값 가져오기
                                String durationAsString = snapshot.child("duration").getValue(String.class);
                                if (durationAsString != null) {
                                    currentTaskTotalDuration = parseDurationInSeconds(durationAsString);
                                    Log.d("FirebaseData", "Current Task Total Duration: " + currentTaskTotalDuration);
                                    //updateCurrentTaskTime(); // 현재 측정 중인 시간으로 업데이트
                                    currentTaskTime.setText(formatDuration(currentTaskTotalDuration));
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                message.setText("측정 시작 전 할 일을 추가하고 선택하세요.");
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
            }
        });
    }


    private void updateTodayTime() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);
        DatabaseReference dateReference = databaseReference.child("totalTime").child(selectedDate);

        dateReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String totalDuration = dataSnapshot.child("totalDuration").getValue(String.class);
                    if (totalDuration != null) {
                        todayTime.setText(formatDurationString(totalDuration));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 이벤트 처리
    }

    // 문자열 형태의 시간을 변환하는 메서드
    private String formatDurationString(String duration) {
        // "h", "m", "s"를 제거하고 각 시간 단위를 추출
        String[] timeUnits = duration.split("\\s+");
        int hours = 0, minutes = 0, seconds = 0;

        for (String unit : timeUnits) {
            if (unit.endsWith("h")) {
                hours = Integer.parseInt(unit.replace("h", ""));
            } else if (unit.endsWith("m")) {
                minutes = Integer.parseInt(unit.replace("m", ""));
            } else if (unit.endsWith("s")) {
                seconds = Integer.parseInt(unit.replace("s", ""));
            }
        }

        // 시, 분, 초를 시간 형식에 맞게 변환
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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

    private WeakReference<Handler> handlerReference = new WeakReference<>(new Handler());
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Handler handler = handlerReference.get();

            if (isRunning) {
                seconds++;
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                stopwatchTextView.setText(time);
                Log.d("347", "stopwatchTextView setted");

                // todayTime 업데이트
                int secondTodayTime = parseStopwatchTextView(todayTime);
                Log.d("이거", String.valueOf(secondTodayTime));
                Log.d("이거", todayTime.getText().toString());

                secondTodayTime++;
                // 시간을 시, 분, 초로 분할
                int hours2 = secondTodayTime / 3600;
                int minutes2 = (secondTodayTime % 3600) / 60;
                int seconds2 = secondTodayTime % 60;

                // "0h 0m 0s" 형태로 포맷팅
                String formattedDuration = String.format("%02d:%02d:%02d", hours2, minutes2, seconds2);
                todayTime.setText(formattedDuration);
                Log.d("이거", formattedDuration);


                // 현재 시간을 SharedPreferences에 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("stopwatchTime", seconds);
                editor.apply();

                updateCurrentTaskTime();

                handler.postDelayed(this, 1000);
            }
        }
    };

    private void startStopwatch() {
        // 현재 선택된 할 일의 이름 가져오기
        String selectedTask = taskTitleSpinner.getSelectedItem().toString();
        currentTaskName.setText(selectedTask);
//        updateCurrentTaskTime(); // 현재 측정 중인 시간으로 업데이트

        Handler handler = handlerReference.get();
        if (handler == null) {
            // WeakReference에서 참조된 핸들러가 이미 해제된 경우 새 핸들러를 생성
            handler = new Handler();
            handlerReference = new WeakReference<>(handler);
        }
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        resetButton.setEnabled(true);
        finishButton.setEnabled(true);
        handler.post(runnable);

        // 현재 측정 중인 할 일의 시작 시간 설정
        measurementStartTime = System.currentTimeMillis();
    }

    private DatabaseReference getTaskReference(String taskName) {
        // Firebase Realtime Database에 데이터 저장
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);
        Log.d("getTaskReference", uid);
        DatabaseReference userTaskReference = databaseReference.child("date").child(selectedDate);
        Log.d("getTaskReference", selectedDate);
        return userTaskReference.orderByChild("taskName").equalTo(taskName).getRef();
    }

    private void updateCurrentTaskTime() {
        Handler handler = handlerReference.get();
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int totalDuration = currentTaskTotalDuration + parseStopwatchTextView(stopwatchTextView);
                    currentTaskTime.setText(formatDuration(totalDuration));

                }
            });
        }
    }

    private String formatDuration(int totalDuration) {
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        int seconds = totalDuration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    private void stopStopwatch() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);
        //updateTodayTime(); // 업데이트 추가
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
        Log.d("461", "stopwatchTextView setted");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Handler handler = handlerReference.get();
        // 핸들러에 등록된 콜백 제거
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // 액티비티가 종료될 때 센서 리스너 등록 해제
        sensorManager.unregisterListener(this);
    }

    // 홈으로 이동하는 메서드
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
        overridePendingTransition(0, R.anim.horizon_exit);
        finish();  // 현재 액티비티 종료
    }

    // 웹 페이지 열기
    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void saveElapsedTimeToDatabase() {
        if (taskTitleSpinner.getSelectedItem() != null) {
            // Spinner에서 선택된 할 일 가져오기
            String selectedTask = taskTitleSpinner.getSelectedItem().toString();

            // Firebase Realtime Database에 데이터 저장
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);
            DatabaseReference userTaskReference = databaseReference.child("date").child(selectedDate);

            // 선택된 할 일에 현재 시간 추가
            userTaskReference.orderByChild("taskName").equalTo(selectedTask).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // 선택된 할 일이 존재하면 시간 업데이트
                        for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                            String taskId = taskSnapshot.getKey();
                            updateTaskDuration(userTaskReference.child(taskId));

                            // 총 학습 시간 업데이트
                            updateTotalStudyTime(databaseReference.child("totalTime").child(selectedDate), userTaskReference);
                            updateTotalStudyTimeForGroup(groupKey, selectedDate, databaseReference);

//                            // 사용자의 그룹 ID를 데이터베이스에서 가져오기
//                            DatabaseReference userGroupReference = databaseReference.child("Group");
//                            userGroupReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot groupSnapshot) {
//                                    if (groupSnapshot.exists()) {
//                                        String groupId = groupSnapshot.getValue(String.class);
//                                        // 사용자가 그룹에 속해 있으면
//                                        if (groupId != null && !groupId.isEmpty()) {
//                                            // 해당 그룹의 총 공부 시간 업데이트
//                                            updateTotalStudyTimeForGroup(groupKey, selectedDate, databaseReference);
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                    Log.e("FirebaseError", "사용자의 그룹 ID를 가져오는 중 오류 발생: " + databaseError.getMessage());
//                                }
//                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
                }
            });
        } else {
            message.setText("측정 시작 전 할 일을 추가하고 선택하세요.");
        }
    }

    private void updateTotalStudyTimeForGroup(String groupId, String date, DatabaseReference databaseReference) {
        DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("Group").child(groupId);
        DatabaseReference groupTotalTimeReference = groupReference.child("totalTime").child(date);

        groupTotalTimeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupSnapshot) {
                int totalDuration = 0;

                // 현재 측정 중인 시간을 초로 변환
                int currentDuration = parseStopwatchTextView(stopwatchTextView);

                // 해당 날짜에 대한 그룹 노드가 있는지 확인
                if (groupSnapshot.exists()) {
                    Log.d("groupSnapshot.exists", "true");

                    totalDuration = parseDurationInSeconds(groupSnapshot.child("totalDuration").getValue(String.class));

                    // 두 시간을 더하여 totalDuration 계산
                    totalDuration += currentDuration;

//                    // 사용자 데이터에서 작업 지속 시간 가져오기
//                    DatabaseReference userTotalTimeReference = databaseReference.child("totalTime").child(date);
//                    userTotalTimeReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String userTaskDuration = snapshot.child("totalDuration").getValue(String.class);
//                            Log.d("userTaskDuration", userTaskDuration);
//                            if (userTaskDuration != null) {
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

                } else {

                    // 두 시간을 더하여 totalDuration 계산
                    totalDuration += currentDuration;

                    // 시간을 시, 분, 초로 분할
                    int hours = totalDuration / 3600;
                    int minutes = (totalDuration % 3600) / 60;
                    int seconds = totalDuration % 60;

                    // "0h 0m 0s" 형태로 포맷팅
                    String formattedDuration = String.format("%dh %dm %ds", hours, minutes, seconds);
                    // 그룹 노드가 없는 경우, 새로운 구조 생성
                    groupReference.child("totalTime").child(date).child("totalDuration").setValue(formattedDuration);
                }
                // 시간을 시, 분, 초로 분할
                int hours = totalDuration / 3600;
                int minutes = (totalDuration % 3600) / 60;
                int seconds = totalDuration % 60;

                // "0h 0m 0s" 형태로 포맷팅
                String formattedDuration = String.format("%dh %dm %ds", hours, minutes, seconds);
                // 그룹의 총 공부 시간 업데이트 또는 생성
                groupTotalTimeReference.child("totalDuration").setValue(formattedDuration);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "그룹의 총 공부 시간을 업데이트하는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }


    private void updateTotalStudyTime(DatabaseReference dateReference, DatabaseReference timeReference) {
        final int[] totalDuration = {0};
        timeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                // 오늘 날짜 노드가 존재하는 경우
                if (dataSnapshot.exists()) {
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        // 각 할일들의 duration 값을 가져와서 합산
                        String durationAsString = taskSnapshot.child("duration").getValue(String.class);
                        if (durationAsString != null) {
                            // 기존 총 학습 시간을 초로 변환
                            totalDuration[0] += parseDurationInSeconds(durationAsString);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dateReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // totalTime 노드가 존재하지 않는 경우
                if (!dataSnapshot.exists()) {
                    dateReference.child("totalDuration").setValue("0h 0m 0s");
                }

                // 현재 측정 중인 시간을 초로 변환
                int currentDuration = parseStopwatchTextView(stopwatchTextView);

                // 두 시간을 더하여 totalDuration 계산
                totalDuration[0] += currentDuration;

                // 시간을 시, 분, 초로 분할
                int hours = totalDuration[0] / 3600;
                int minutes = (totalDuration[0] % 3600) / 60;
                int seconds = totalDuration[0] % 60;

                // "0h 0m 0s" 형태로 포맷팅
                String formattedDuration = String.format("%dh %dm %ds", hours, minutes, seconds);

                // Firebase 데이터베이스에 저장
                dateReference.child("totalDuration").setValue(formattedDuration);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }


    private void updateTaskDuration(DatabaseReference taskReference) {
        taskReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // "duration" 필드가 dataSnapshot에 있는지 확인
                    if (dataSnapshot.hasChild("duration")) {
                        // "h", "m", "s"를 제거하고 각 시간 단위를 추출
                        String durationAsString = dataSnapshot.child("duration").getValue(String.class);
                        if (durationAsString != null) {

                            // stopwatchTextView에 표시된 시간을 파싱하여 시, 분, 초로 분할
                            int currentDuration = parseStopwatchTextView(stopwatchTextView);

                            // 기존 지속 시간을 초로 변환
                            int existingDuration = parseDurationInSeconds(durationAsString);

                            // 두 시간을 더하여 totalDuration 계산
                            int totalDuration = existingDuration + currentDuration;

                            saveTaskDurationToFirebase(totalDuration, taskReference);

                            // SharedPreferences에서 스톱워치 시간 초기화
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("stopwatchTime", 0);
                            editor.apply();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Firebase 데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }
    // stopwatchTextView에 표시된 시간을 초로 변환하는 메서드
    private int parseStopwatchTextView(TextView textView) {
        String timeString = textView.getText().toString();
        String[] timeUnits = timeString.split(":");
        int hours = Integer.parseInt(timeUnits[0]);
        int minutes = Integer.parseInt(timeUnits[1]);
        int seconds = Integer.parseInt(timeUnits[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    // 시간 문자열을 파싱하여 초로 변환하는 메서드
    private int parseDurationInSeconds(String durationAsString) {
        String[] timeUnits = durationAsString.split("\\s+");

        int hours = 0, minutes = 0, seconds = 0;

        for (String unit : timeUnits) {
            if (unit.endsWith("h")) {
                hours = Integer.parseInt(unit.replace("h", ""));
            } else if (unit.endsWith("m")) {
                minutes = Integer.parseInt(unit.replace("m", ""));
            } else if (unit.endsWith("s")) {
                seconds = Integer.parseInt(unit.replace("s", ""));
            }
        }

        // 시, 분, 초를 초 단위로 변환하여 반환
        return hours * 3600 + minutes * 60 + seconds;
    }

    private void saveTaskDurationToFirebase(int totalDuration, DatabaseReference taskReference) {
        // currentTaskTotalDuration 업데이트
        currentTaskTotalDuration = totalDuration;

        // 시간을 시, 분, 초로 분할
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        int seconds = totalDuration % 60;

        // "0h 0m 0s" 형태로 포맷팅
        String formattedDuration = String.format("%dh %dm %ds", hours, minutes, seconds);

        // Firebase 데이터베이스에 저장
        taskReference.child("duration").setValue(formattedDuration);
    }


}