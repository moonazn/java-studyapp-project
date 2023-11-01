package com.cookandroid.studyapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimerActivity extends Activity {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button setDeadlineButton;
    private TextView remainingTimeTextView;

    // SharedPreferences 키
    private static final String PREF_NAME = "TimerPrefs";
    private static final String DEADLINE_KEY = "deadlineTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        setDeadlineButton = findViewById(R.id.setDeadlineButton);
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);

        // 데드라인 불러오기
        long savedDeadline = loadDeadline();
        if (savedDeadline > 0) {
            showRemainingTime(savedDeadline);
        }

        setDeadlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadline();
            }
        });
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