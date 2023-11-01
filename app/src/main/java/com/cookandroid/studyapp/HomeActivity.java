package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private ToggleButton toggleButton;
    private EditText editTextGoal;
    private EditText editTextGoal2;
    private ImageView plusButton;
    private ImageView plusButton2;
    private ImageView stopwatchButton;

    private ImageView timerButton;
    private CalendarView calendarViewMonthly;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        home.setAlpha(1f);

        home.setOnClickListener(v -> {
            if (!getClass().equals(HomeActivity.class)) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        plusButton = findViewById(R.id.plus_additional);
        editTextGoal = findViewById(R.id.et_memo);

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ListActivity.class);
                startActivity(intent);
                // 클릭 이벤트 처리 코드 추가
                String userGoal = editTextGoal.getText().toString();
                if (!userGoal.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "User Todo: " + userGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter your Todo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        plusButton2 = findViewById(R.id.plus_additional2);
        //editTextGoal2 = findViewById(R.id.et_memo_goal);

        plusButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
                startActivity(intent);
                // 클릭 이벤트 처리 코드 추가
                String userGoal = editTextGoal2.getText().toString();
                if (!userGoal.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "User Goal: " + userGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter your goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopwatchButton = findViewById(R.id.stopwatch);
        stopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddStopwatchActivity.class);
                startActivity(intent);
            }
        });

        timerButton = findViewById(R.id.timer);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TimerActivity.class);
                startActivity(intent);
            }
        });


        calendarViewMonthly = findViewById(R.id.calendarViewMonthly);
        calendarViewMonthly.setShownWeekCount(1);

        // 사용자의 그룹 정보를 가져오고 RecyclerView에 멤버 목록을 표시
        getGroupKeyForUser("사용자_닉네임"); // 사용자_닉네임을 실제 닉네임으로 대체

        // RecyclerView에 어댑터를 연결
        List<String> sampleMemberList = new ArrayList<>(); // 사용자의 멤버 목록 데이터가 필요함
        initRecyclerView(sampleMemberList); // 샘플 데이터 대신 실제 멤버 목록 데이터를 전달
    }

    private void getGroupKeyForUser(String nickname) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        groupsRef.orderByChild("members/" + nickname).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 사용자가 속한 스터디 그룹(들)을 찾았을 때
                    List<String> groupKeys = new ArrayList<>();
                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        String groupKey = groupSnapshot.getKey();
                        groupKeys.add(groupKey);
                    }

                    // 각 스터디 그룹의 멤버 목록을 가져와 RecyclerView를 업데이트
                    for (String groupKey : groupKeys) {
                        loadGroupMembersAndUpdateRecyclerView(groupKey);
                    }
                } else {
                    // 사용자가 어떤 스터디 그룹에도 속해있지 않을 때 처리
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    private void loadGroupMembersAndUpdateRecyclerView(String groupKey) {
        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("GroupMembers");
        groupMembersRef.child(groupKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> memberList = new ArrayList<>();
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                        String memberName = memberSnapshot.getValue(String.class);
                        memberList.add(memberName);
                    }

                    // RecyclerView 업데이트
                    initRecyclerView(memberList);
                } else {
                    // 그룹에 멤버가 없거나 데이터가 없을 때 처리
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    private void initRecyclerView(List<String> memberList) {
        RecyclerView recyclerView = findViewById(R.id.memberRecyclerView);
        MemberAdapter memberAdapter = new MemberAdapter(memberList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(memberAdapter); // RecyclerView에 어댑터 연결
    }

}
