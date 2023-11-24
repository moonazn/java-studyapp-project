package com.cookandroid.studyapp;

// AlarmActivity.java
import com.cookandroid.studyapp.SwipeToDeleteCallback;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        //스와이프 삭제
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // position이 유효한지 확인합니다.
                if (position != RecyclerView.NO_POSITION) {
                    // 알람을 취소하고 목록에서 제거합니다.
                    cancelAlarm(position);

//                    // 목록에서 제거하기 전에 크기를 확인합니다.
//                    if (position < alarmList.size()) {
//                        alarmList.remove(position);
//                        alarmAdapter.updatedata(alarmList);
//                        alarmAdapter.notifyItemRemoved(position);
//                    }
                }
            }

        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);

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
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            int hour = data.getIntExtra("ALARM_HOUR", 0);
            int minute = data.getIntExtra("ALARM_MINUTE", 0);
            String mission = data.getStringExtra("SELECTED_MISSION");

            int uniqueId = generateUniqueId();  // 고유 ID 생성 메서드를 호출하여 ID를 얻어옵니다.
            alarmList.add(new AlarmInfo(uniqueId, hour, minute, mission));
            alarmAdapter.notifyDataSetChanged();
        }
    }

    // Adapter 클래스 추가
    public static class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

        private List<AlarmInfo> alarmList;

        public AlarmAdapter(List<AlarmInfo> alarmList) {
            this.alarmList = alarmList;
        }

        public void updatedata(List<AlarmInfo> alarmList){
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
            holder.alarmTextView.setText("알람 시간 : " + alarm.getHour() + "시 " + alarm.getMinute() + "분"+"\n" +"미션 : "+alarm.getMission());

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
    private void cancelAlarm(int position) {
        AlarmInfo deletedAlarm = alarmList.get(position);

        // 액티비티에서 호출되었음을 나타내기 위해 true 전달
        AlarmReceiver.cancelAlarm(this, deletedAlarm.getId(), true);

        alarmList.remove(deletedAlarm);
        alarmAdapter.updatedata(alarmList);
        alarmAdapter.notifyDataSetChanged();

        Log.d("AlarmActivity", "알람 삭제!: " + deletedAlarm.getId());
    }





    @RequiresApi(api = Build.VERSION_CODES.N)
    private int generateUniqueId() {
        AtomicInteger counter = new AtomicInteger(0);
        alarmList.forEach(alarmInfo -> counter.updateAndGet(v -> Math.max(v, alarmInfo.getId() + 1)));
        return counter.get();
    }

    public static class AlarmInfo {
        private int id;  // 알람을 식별하기 위한 고유 ID
        private int hour;
        private int minute;
        private String mission;

        public AlarmInfo(int id, int hour, int minute, String mission) {
            this.id = id;
            this.hour = hour;
            this.minute = minute;
            this.mission = mission;
        }

        public int getId() {
            return id;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public String getMission() {
            return mission;
        }
    }

}