package com.cookandroid.studyapp;

// AlarmActivity.java

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {

    private List<AlarmInfo> alarmList = new ArrayList<>();
    private AlarmAdapter alarmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmAdapter = new AlarmAdapter(alarmList);
        recyclerView.setAdapter(alarmAdapter);

        // 플러스 버튼 클릭 이벤트 처리
        Button plusButton = findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 클릭 이벤트 처리
                Intent intent = new Intent(AlarmActivity.this, AlarmPlusActivity.class);
                startActivityForResult(intent, 1); // 알람 플러스 액티비티 호출 및 결과 처리
            }
        });

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        alarm.setAlpha(1f);

        home.setOnClickListener(v -> {
            if (!getClass().equals(HomeActivity.class)) {
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

    // 알람 플러스 액티비티에서 결과를 받아올 때 호출되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            int hour = data.getIntExtra("ALARM_HOUR", 0);
            int minute = data.getIntExtra("ALARM_MINUTE", 0);
            alarmList.add(new AlarmInfo(hour, minute));
            alarmAdapter.notifyDataSetChanged();
        }
    }

    // Adapter 클래스 추가
    private static class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

        private List<AlarmInfo> alarmList;

        public AlarmAdapter(List<AlarmInfo> alarmList) {
            this.alarmList = alarmList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.alarm_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            AlarmInfo alarm = alarmList.get(position);
            holder.alarmTextView.setText("알람 시간: " + alarm.getHour() + "시 " + alarm.getMinute() + "분");

            // 삭제 버튼 클릭 이벤트 처리
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarmList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return alarmList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView alarmTextView;
            public Button deleteButton;

            public ViewHolder(View view) {
                super(view);
                alarmTextView = view.findViewById(R.id.alarmTextView);
                deleteButton = view.findViewById(R.id.deleteButton);
            }
        }
    }

    public static class AlarmInfo {
        private int hour;
        private int minute;

        public AlarmInfo(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }
    }
}