package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JoinInfoActivity extends AppCompatActivity {

    // Firebase Authentication 인스턴스 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_info);

        // Firebase Realtime Database 인스턴스 가져오기
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

// 사용자 정보 저장

        EditText nicknameEditText = findViewById(R.id.name_area);

        TextView message = findViewById(R.id.message);

        Button joinBut = findViewById(R.id.join_button);

        joinBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameEditText.getText().toString();

                if (TextUtils.isEmpty(nickname)) {
                    // 닉네임이 비어 있으면 경고 표시
                    message.setText("닉네임을 입력하세요.");
                } else {
                    String userId = mAuth.getCurrentUser().getUid();
//                    User user = new User(nickname);
                    usersRef.child(userId).setValue(nickname);

                    Intent intent = new Intent(JoinInfoActivity.this, MemberAddActivity.class);
                    startActivity(intent);

                }
            }
        });

    }
}