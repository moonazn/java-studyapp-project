package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.RandomNameGenerator.generateRandomName;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupNamePopup extends Dialog {
    private EditText editGroupName;
    private ImageView random;
    private TextView message;
    private Button btnCreateGroup;
    private Button btnCancel;

    public GroupNamePopup(Context context) {
        super(context);
        setContentView(R.layout.group_name_popup);

        editGroupName = findViewById(R.id.editGroupName);
        random = findViewById(R.id.randomButton);
        message = findViewById(R.id.message);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnCancel = findViewById(R.id.btnCancel);

        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String randomName;
                RandomNameGenerator.generateRandomName(new RandomNameGenerator.RandomNameCallback() {
                    @Override
                    public void onSuccess(String randomName) {
                        // 랜덤 이름을 성공적으로 받았을 때 실행되는 코드
                        editGroupName.setText(randomName);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // 작업 중 오류가 발생했을 때 실행되는 코드
                        editGroupName.setText("random name Error");
                    }
                });

            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 그룹 생성 후, 그룹 멤버로 현재 로그인된 사용자 추가
                String groupName = editGroupName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    // 그룹 이름이 비어 있으면 경고 표시
                    message.setText("스터디 이름을 입력하세요.");
                } else {
                    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

                    // 그룹 이름 중복 확인
                    groupsRef.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // 이미 존재하는 그룹 이름인 경우 처리
                                message.setText("이미 사용 중인 스터디 이름입니다.");
                            } else {
                                // 중복되지 않는 그룹 이름인 경우
                                String groupKey = groupsRef.push().getKey();

                                // 새로운 그룹을 위한 고유한 키 생성

                                // 그룹 이름 저장
                                groupsRef.child(groupKey).child("groupName").setValue(groupName);

                                // Firebase Authentication으로 현재 로그인한 사용자의 UID 가져오기
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                String userId = currentUser.getUid(); // 현재 로그인한 사용자의 UID

                                // 업로드 데이터에 있는 유저 UID를 사용하여 유저 닉네임을 가져옵니다.
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String userNickname = dataSnapshot.getValue(String.class);

                                            // 그룹에 현재 로그인한 사용자 추가
                                            groupsRef.child(groupKey).child("members").child(userNickname).setValue(true);

                                            DatabaseReference defaultGroupRef = FirebaseDatabase.getInstance().getReference("Group")
                                                    .child("-Ngn8da9xL4ZCKhgqwdq").child("members");

                                            // 사용자 ID를 default 그룹의 members 노드에서 삭제
                                            defaultGroupRef.child(userNickname).removeValue()
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            // 멤버 제거가 성공한 경우
                                                            Log.d("test", "noGroup 멤버 제거 성공");
                                                        } else {
                                                            // 멤버 제거 중 오류가 발생한 경우
                                                            Log.d("test", "noGroup 멤버 제거 실패");
                                                        }
                                                    });

                                            // 그룹 생성 후, 팝업을 닫을 수 있습니다.
                                            dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle the event of the database operation being canceled
                                        if (error != null) {
                                            // Log the error or take appropriate action
                                            Log.e("FirebaseError", "Database operation canceled: " + error.getMessage());
                                        }
                                    }
                                });
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


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
