package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MemberAddActivity extends AppCompatActivity {

    String newMember;
    GroupNamePopup groupNamePopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memberadd);

        EditText editMember = findViewById(R.id.nickname);
        TextView message = findViewById(R.id.message);

        Button addButton = findViewById(R.id.addButton);
        TextView newGroupButton = findViewById(R.id.newGroupButton);
        TextView startButton = findViewById(R.id.startButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMember = editMember.getText().toString();

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                usersRef.orderByChild("nickname").equalTo(newMember).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean userExists = dataSnapshot.exists();

                        if (userExists) {
                            // 존재하는 유저인 경우 처리
                            checkUserAndAddToGroup(newMember);
                        } else {
                            message.setText("존재하지 않는 유저입니다.");
                            message.setTextColor(Color.parseColor("#FF0040"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 오류 처리
                    }
                });
            }
        });


        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupNamePopup = new GroupNamePopup(MemberAddActivity.this);
                groupNamePopup.show();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberAddActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_exit);
            }
        });
    }

    // 사용자가 속한 그룹을 찾고 그룹에 멤버를 추가하는 메서드
    private void checkUserAndAddToGroup(String newMember) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserUid = currentUser.getUid();


        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 사용자의 UID를 기반으로 해당 사용자의 닉네임 가져오기
        usersRef.child("-"+currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String currentUserNickname = dataSnapshot.child("nickname").getValue(String.class);

                    // 현재 로그인한 유저(A)가 속한 스터디 그룹이 어디인지 찾기
                    getGroupKeyForUser(currentUserUid, currentUserNickname, newMember);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });
    }

    // 사용자가 속한 그룹 키를 찾고 멤버를 추가하는 메서드
    private void getGroupKeyForUser(String userUid, String currentUserNickname, String newMember) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        TextView message = findViewById(R.id.message);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                String groupKey = null;

                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(currentUserNickname)) {
                        // 사용자가 속한 그룹을 찾음
                        groupKey = groupSnapshot.getKey();
                        break;
                    }
                }

                if (groupKey != null) {
                    // 그룹을 찾았을 경우, 멤버를 추가
                    DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("Group")
                            .child(groupKey).child("members");

                    groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot membersSnapshot) {
                            if (membersSnapshot.hasChild(newMember)) {
                                message.setText(newMember + "님이 이미 그룹의 멤버입니다.");
                                message.setTextColor(Color.parseColor("#FF0040"));
                            } else {
                                // 그룹의 "members" 노드에 멤버를 추가
                                groupMembersRef.child(newMember).setValue(true, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            message.setText(newMember + "님이 그룹의 멤버로 추가되었습니다.");
                                            message.setTextColor(Color.parseColor("#4000FF"));

                                            DatabaseReference defaultGroupRef = FirebaseDatabase.getInstance().getReference("Group")
                                                    .child("-Ngn8da9xL4ZCKhgqwdq").child("members");

                                            // 사용자 ID를 default 그룹의 members 노드에서 삭제
                                            defaultGroupRef.child(newMember).removeValue()
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            // 멤버 제거가 성공한 경우
                                                            Log.d("test", "noGroup 멤버 제거 성공");
                                                        } else {
                                                            // 멤버 제거 중 오류가 발생한 경우
                                                            Log.d("test", "noGroup 멤버 제거 실패");
                                                        }
                                                    });
                                        } else {
                                            message.setText("멤버 추가 중 오류가 발생했습니다.");
                                            message.setTextColor(Color.parseColor("#FF0040"));

                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 오류 처리
                        }
                    });
                } else {
                    // 그룹 키가 없을 때의 처리
                    message.setText("현재 사용자가 속한 스터디 그룹이 존재하지 않습니다.");
                    message.setTextColor(Color.parseColor("#FF0040"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }
}
