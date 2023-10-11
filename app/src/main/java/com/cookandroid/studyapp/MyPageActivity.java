package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyPageActivity extends AppCompatActivity {

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();

    static String nickname = ""; // 닉네임을 저장할 변수

    private String greetWithName = "";

    private TextView helloText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        helloText = findViewById(R.id.greetText);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

// 사용자의 UID를 기반으로 해당 사용자의 닉네임 가져오기
        if (nickname == "") {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nickname = dataSnapshot.getValue(String.class);
                        Log.d("test", "nickname : "+ nickname);

                        // 데이터를 가져온 후에 UI를 업데이트
                        updateUIWithNickname();
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
        } else {
            // 이미 nickname이 존재하면 UI를 업데이트
            updateUIWithNickname();
        }



        Button logout = findViewById(R.id.logoutButton);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Firebase에서 로그아웃
                mAuth.signOut();

                nickname = "";

                // 로그아웃 후 처리 (예: 다른 화면으로 이동)
                Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        myPage.setAlpha(1f);

        // 바텀 바 이동 이벤트
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    // 데이터를 가져온 후에 UI를 업데이트
    void updateUIWithNickname(){
        helloText.setText(nickname + "님, 반가워요!");
    }
}