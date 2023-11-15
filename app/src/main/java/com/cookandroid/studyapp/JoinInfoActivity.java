package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

                    // 데이터베이스에서 중복 닉네임 확인
                    usersRef.orderByValue().equalTo(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // 이미 존재하는 닉네임인 경우 처리
                                message.setText("이미 사용 중인 닉네임입니다.");
                            } else {
                                // 중복되지 않는 닉네임인 경우
                                usersRef.child("-" + userId).child("nickname").setValue(nickname);
                                addMemberToDefaultGroup(nickname);

                                Intent intent = new Intent(JoinInfoActivity.this, MemberAddActivity.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // 오류 처리
                        }
                    });

                }
            }
        });

    }

    // 회원가입 시 실행
    public static void addMemberToDefaultGroup(String nickname) {
        DatabaseReference defaultGroupRef = FirebaseDatabase.getInstance().getReference("Group")
                .child("-Ngn8da9xL4ZCKhgqwdq").child("members");

        // 사용자 ID를 default 그룹의 members 노드에 추가
        defaultGroupRef.child(nickname).setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // 멤버 추가가 성공한 경우
                    Log.d("text", "noGroup 멤버추가 성공");
                } else {
                    // 멤버 추가 중 오류가 발생한 경우
                    Log.d("text", "noGroup 멤버추가 실패");
                }
            }
        });
    }

}