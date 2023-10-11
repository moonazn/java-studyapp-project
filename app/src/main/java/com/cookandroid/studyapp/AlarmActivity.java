package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Spinner missionSpinner, repeatSpinner;
    private CheckBox checkboxMonday, checkboxTuesday, checkboxWednesday, checkboxThursday, checkboxFriday, checkboxSaturday, checkboxSunday;
    private int hour; // 시간을 저장할 변수
    private int minute; // 분을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // 뷰 초기화
        timePicker = findViewById(R.id.timePicker);
        missionSpinner = findViewById(R.id.missionSpinner);
        repeatSpinner = findViewById(R.id.repeatSpinner);
        checkboxMonday = findViewById(R.id.checkboxMonday);
        checkboxTuesday = findViewById(R.id.checkboxTuesday);
        checkboxWednesday = findViewById(R.id.checkboxWednesday);
        checkboxThursday = findViewById(R.id.checkboxThursday);
        checkboxFriday = findViewById(R.id.checkboxFriday);
        checkboxSaturday = findViewById(R.id.checkboxSaturday);
        checkboxSunday = findViewById(R.id.checkboxSunday);

        // 반복 주기 스피너 초기화
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(this, R.array.repeat_array, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        // 저장 버튼 클릭 이벤트 처리
        Button saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 시간 선택 처리
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfDay) {
                        // 선택한 시간과 분을 변수에 저장
                        hour = hourOfDay;
                        minute = minuteOfDay;
                    }
                });

                // 미션 선택 처리
                String selectedMission = missionSpinner.getSelectedItem().toString();

                // 선택된 요일 처리
                StringBuilder selectedDays = new StringBuilder();
                if (checkboxMonday.isChecked()) selectedDays.append("월요일, ");
                if (checkboxTuesday.isChecked()) selectedDays.append("화요일, ");
                if (checkboxWednesday.isChecked()) selectedDays.append("수요일, ");
                if (checkboxThursday.isChecked()) selectedDays.append("목요일, ");
                if (checkboxFriday.isChecked()) selectedDays.append("금요일, ");
                if (checkboxSaturday.isChecked()) selectedDays.append("토요일, ");
                if (checkboxSunday.isChecked()) selectedDays.append("일요일, ");

                // 선택된 반복 주기 처리
                String selectedRepeat = repeatSpinner.getSelectedItem().toString();

                // 결과 메시지 생성
                String resultMessage = "설정된 시간: " + hour + ":" + minute + "\n" +
                        "선택된 미션: " + selectedMission + "\n" +
                        "선택된 요일: " + selectedDays.toString() + "\n" +
                        "반복 주기: " + selectedRepeat;

                // 결과 메시지를 토스트로 표시
                Toast.makeText(AlarmActivity.this, resultMessage, Toast.LENGTH_LONG).show();
            }
        });


        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        alarm.setAlpha(1f);

        // 바텀 바 이동 이벤트
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}