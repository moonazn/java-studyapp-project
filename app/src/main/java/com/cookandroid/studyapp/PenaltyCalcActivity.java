package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;
import static com.cookandroid.studyapp.MyPageActivity.nickname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PenaltyCalcActivity extends AppCompatActivity {

    private PenaltyMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penalty_calc);

        getGroupKeyForUser(nickname);

        TextView back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 전환을 위한 Intent 생성
                Intent intent = new Intent(PenaltyCalcActivity.this, HomeActivity.class);

                // Intent를 사용하여 화면을 전환합니다.
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });


    }


    private void getGroupKeyForUser(String currentUserNickname) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(currentUserNickname)) {
                        // 사용자가 속한 그룹을 찾음
                        Log.d("test", "groupKey : " + groupKey);

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
    // 데이터 로딩을 완료한 후 RecyclerView를 초기화
    private void initRecyclerView(List<String> memberList) {
        RecyclerView memberRecyclerView = findViewById(R.id.memberRecyclerView);

        if (memberList.isEmpty()) {
            // 데이터가 비어있는 경우에 대한 처리
            // 예를 들어, 사용자에게 메시지를 표시할 수 있습니다.
        } else {
            // 데이터가 준비된 경우 어댑터를 생성하고 연결
            memberAdapter = new PenaltyMemberAdapter(memberList);
            memberRecyclerView.setLayoutManager(new LinearLayoutManager(PenaltyCalcActivity.this, LinearLayoutManager.VERTICAL, false));
            memberRecyclerView.setAdapter(memberAdapter);
        }
    }

}