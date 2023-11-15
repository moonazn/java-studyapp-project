package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.nickname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();

    private MemberAdapter memberAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);
        ImageView plusAdditional1 = findViewById(R.id.plus_additional1);
        ImageView plusAdditional2 = findViewById(R.id.plus_additional2);
        ImageView plusAdditional3 = findViewById(R.id.plus_additional3);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child("-"+uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("test", "nickname: " + nickname);

                    // 스터디 멤버 리스트 업데이트
                    getGroupMemForUser(nickname);
                } else {
                    // 사용자의 UID에 해당하는 데이터가 없을 때 처리
                    Log.d("test", "DataSnapshot does not exist for UID: " + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("test", "onCancelled called");
            }
        });

        TextView penaltyCalc = findViewById(R.id.penaltyCalc);

        penaltyCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PenaltyCalcActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });



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

        plusAdditional1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, TodoActivity.class);
                startActivity(intent);
            }
        });

        plusAdditional2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
                startActivity(intent);
            }
        });

        plusAdditional3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, StopwatchActivity.class);
                startActivity(intent);
            }
        });

    }

    // 아래에 onAdditionalWatchClick 및 onAdditionalTimerClick 메소드를 추가
    public void onAdditionalWatchClick(View view) {
        Intent intent = new Intent(HomeActivity.this, StopwatchActivity.class);
        startActivity(intent);
    }

    public void onAdditionalTimerClick(View view) {
        Intent intent = new Intent(HomeActivity.this, GoalActivity.class);
        startActivity(intent);
    }

    // 데이터 로딩을 완료한 후 RecyclerView를 초기화
    private void initRecyclerView(List<String> memberList) {
        RecyclerView memberRecyclerView = findViewById(R.id.memberRecyclerView);

        if (memberList.isEmpty()) {
            // 데이터가 비어있는 경우에 대한 처리
            // 예를 들어, 사용자에게 메시지를 표시할 수 있습니다.
        } else {
            // 데이터가 준비된 경우 어댑터를 생성하고 연결
            memberAdapter = new MemberAdapter(memberList);
            memberRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
            memberRecyclerView.setAdapter(memberAdapter);
        }
    }

    private void getGroupMemForUser(String currentUserNickname) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(currentUserNickname)) {

                        // 멤버 목록을 가져온 후 RecyclerView 초기화
                        List<String> memberList = new ArrayList<>();
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            String memberName = memberSnapshot.getKey();
                            memberList.add(memberName);
                        }
                        initRecyclerView(memberList);
                        break; // 그룹을 찾았으면 루프 종료
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }
}