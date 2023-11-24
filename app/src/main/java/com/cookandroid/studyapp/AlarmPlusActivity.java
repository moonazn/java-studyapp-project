package com.cookandroid.studyapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AlarmPlusActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Spinner missionSpinner, repeatSpinner;
    private CheckBox checkboxMonday, checkboxTuesday, checkboxWednesday, checkboxThursday, checkboxFriday, checkboxSaturday, checkboxSunday;

    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_plus);

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

        // ArrayAdapter를 초기화하고 스피너에 설정
        ArrayAdapter<CharSequence> missionAdapter = ArrayAdapter.createFromResource(this, R.array.mission_array, android.R.layout.simple_spinner_item);
        missionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        missionSpinner.setAdapter(missionAdapter);

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
                int hourOfDay = timePicker.getCurrentHour();
                int minuteOfDay = timePicker.getCurrentMinute();

                // 선택된 미션
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
                //selectedMission = missionSpinner.getSelectedItem().toString();


                // 결과 메시지 생성
                String resultMessage = "설정된 시간: " + hourOfDay + "시" + minuteOfDay + "분" + "\n" +
                        "선택된 미션: " + selectedMission + "\n" +
                        "선택된 요일: " + selectedDays.toString() + "\n" +
                        "반복 주기: " + selectedRepeat;

                // 결과 메시지를 토스트로 표시
                Toast.makeText(AlarmPlusActivity.this, resultMessage, Toast.LENGTH_LONG).show();

                // 알람 설정 메서드 호출
                setAlarm(hourOfDay, minuteOfDay,selectedMission);

                // 알람 시간을 AlarmActivity로 전달
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ALARM_HOUR", hourOfDay);
                resultIntent.putExtra("ALARM_MINUTE", minuteOfDay);
                resultIntent.putExtra("SELECTED_MISSION", selectedMission);



                setResult(RESULT_OK, resultIntent);
                finish(); // 현재 액티비티 종료
            }
        });

        // 테스트 버튼 클릭 이벤트 처리
        testButton = findViewById(R.id.TestButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 클릭 이벤트 처리
                Intent intent = new Intent(AlarmPlusActivity.this, PracticeMissionActivity.class);
                startActivity(intent);
            }
        });

        // 팝업 버튼 클릭 이벤트 처리
        Button showPopupButton = findViewById(R.id.showPopupButton);
        showPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업을 띄우기 위한 다이얼로그
                final Dialog popupDialog = new Dialog(AlarmPlusActivity.this);
                popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popupDialog.setContentView(R.layout.popup_layout);

                // 팝업 내용 초기화
                TextView popupTextView = popupDialog.findViewById(R.id.popupTextView);
                popupTextView.setText("타일 맞추기 : 1~25의 타일을 순서에 맞게 터치해서 없애요!" + "\n"
                        +"숫자 계산 : 주어진 숫자들의 사칙 연산을 알맞게 하세요!" + "\n"
                        + "명언 쓰기 : 주어진 명언들을 종이에 적어 사진을 찍어서 인증하세요!");


                // 팝업 닫기 버튼 처리
                Button closePopupButton = popupDialog.findViewById(R.id.closePopupButton);
                closePopupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDialog.dismiss(); // 팝업 닫기
                    }
                });

                // 팝업 표시
                popupDialog.show();
            }
        });
    }

    private void setAlarm(int hour, int minute, String mission) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);

        // 소리 변경을 위한 Uri
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarmsound);

        // 소리 설정
        alarmIntent.putExtra("android.media.extra.NOTIFICATION", soundUri);
        alarmIntent.putExtra("android.media.extra.STREAM_TYPE", AudioManager.STREAM_ALARM);

        // 알람 시간 설정
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);

        // 알람 시간 및 미션 설정
        alarmIntent.putExtra("ALARM_HOUR", hour);
        alarmIntent.putExtra("ALARM_MINUTE", minute);
        alarmIntent.putExtra("SECOND", 0);
        alarmIntent.putExtra("MISSION", mission);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // 알람 설정
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);

        // 백그라운드에서 알람 울리기 위한 Service 시작
        Intent serviceIntent = new Intent(this, AlarmService.class);
        // 백그라운드에서 소리 설정
        serviceIntent.putExtra("android.media.extra.NOTIFICATION", Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarmsound));
        serviceIntent.putExtra("ALARM_HOUR", hour);
        serviceIntent.putExtra("ALARM_MINUTE", minute);
        serviceIntent.putExtra("MISSION", mission);
        startService(serviceIntent);
    }
    public void stopAlarm() {
        // 알람을 취소하기 위한 Intent 생성
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // AlarmManager를 이용하여 알람 취소
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        // 백그라운드에서 울리는 서비스 종료
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
    }

}