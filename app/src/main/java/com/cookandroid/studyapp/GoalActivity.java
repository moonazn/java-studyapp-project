package com.cookandroid.studyapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class GoalActivity extends AppCompatActivity {
    private EditText etGoalMemo;
    private ListView listViewGoals;
    private ArrayList<String> goalList;
    private ArrayAdapter<String> goalAdapter;
    private Button buttonAddGoal;
    private int maxMemoLength = 20;

    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button setDeadlineButton;
    private TextView remainingTimeTextView;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    // SharedPreferences 키
    private static final String PREF_NAME = "TimerPrefs";
    private static final String DEADLINE_KEY = "deadlineTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        etGoalMemo = findViewById(R.id.etGoalMemo);
        listViewGoals = findViewById(R.id.listViewGoals);
        buttonAddGoal = findViewById(R.id.buttonAddGoal);

        goalList = new ArrayList<>();
        goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goalList);
        listViewGoals.setAdapter(goalAdapter);

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("goals");

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        setDeadlineButton = findViewById(R.id.setDeadlineButton);
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);

        etGoalMemo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > maxMemoLength) {
                    etGoalMemo.setText(charSequence.subSequence(0, maxMemoLength));
                    etGoalMemo.setSelection(maxMemoLength);
                    Toast.makeText(GoalActivity.this, "최대 " + maxMemoLength + "글자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        buttonAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String memo = etGoalMemo.getText().toString().trim();
                if (!memo.isEmpty()) {
                    // Firebase Realtime Database에 목표 추가
                    String goalId = databaseReference.push().getKey();
                    databaseReference.child(goalId).setValue(memo);
                    goalList.add(memo); // 목표 목록에도 추가
                    goalAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알림

                    etGoalMemo.setText("");
                } else {
                    Toast.makeText(GoalActivity.this, "목표를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setDeadlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadline();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setDeadline() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        long deadlineMillis = calendar.getTimeInMillis();

        // 데드라인 저장
        saveDeadline(deadlineMillis);

        showRemainingTime(deadlineMillis);
    }

    private void saveDeadline(long deadlineMillis) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DEADLINE_KEY, deadlineMillis);
        editor.apply();
    }

    private long loadDeadline() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getLong(DEADLINE_KEY, 0);
    }

    private void showRemainingTime(long deadlineMillis) {
        // 현재 시간
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis >= deadlineMillis) {
            // 디데이가 이미 지났을 경우
            remainingTimeTextView.setText("디데이가 이미 지났습니다.");
        } else {
            // 디데이가 아직 도달하지 않은 경우, 디데이까지의 시간 계산
            long timeRemaining = deadlineMillis - currentTimeMillis;

            // 남은 일수와 시간 계산
            int days = (int) (timeRemaining / (1000 * 60 * 60 * 24));
            int hours = (int) ((timeRemaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            int minutes = (int) ((timeRemaining % (1000 * 60 * 60)) / (1000 * 60));

            String remainingTime = "디데이까지 " + days + "일 " + hours + "시간 " + minutes + "분 남았습니다.";
            remainingTimeTextView.setText(remainingTime);
        }
    }
}