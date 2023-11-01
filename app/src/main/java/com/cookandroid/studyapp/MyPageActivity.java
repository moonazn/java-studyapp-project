package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPageActivity extends AppCompatActivity {

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();

    static String nickname = ""; // 닉네임을 저장할 변수

    static String groupKey = "";
    private TextView helloText;

    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        helloText = findViewById(R.id.greetText);

        ImageView editImage = findViewById(R.id.editButton);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 사용자의 UID를 기반으로 해당 사용자의 닉네임 가져오기
        if (nickname.equals("")) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nickname = dataSnapshot.getValue(String.class);
                        Log.d("test", "nickname: " + nickname);

                        // 데이터를 가져온 후에 UI를 업데이트
                        updateUIWithNickname();

                        // 스터디 멤버 리스트 업데이트
                        getGroupKeyForUser(nickname);
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

            // 스터디 멤버 리스트 업데이트
            getGroupKeyForUser(nickname);
        }

        TextView editFinish = findViewById(R.id.editFinish);

        // "편집" 버튼 클릭 이벤트 처리
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (memberAdapter != null) {
                    boolean editMode = !memberAdapter.isEditMode();
                    memberAdapter.setEditMode(editMode);

                    if (editMode) {
                        editImage.setImageResource(R.drawable.add); // "추가" 버튼 아이콘으로 변경
                        editFinish.setVisibility(View.VISIBLE);

                        editFinish.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                editFinish.setVisibility(View.GONE);
                                memberAdapter.setEditMode(false);
                                editImage.setImageResource(R.drawable.pen); // "편집" 버튼 아이콘으로 변경
                            }
                        });
                    } else {
                        // "추가" 버튼 클릭 이벤트 처리
                        Intent intent = new Intent(MyPageActivity.this, MemberAddActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                }
            }
        });

        Button appLock = findViewById(R.id.rockApps);

        appLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, AppLockActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        TextView groupOut = findViewById(R.id.groupOutButton);
        Button logout = findViewById(R.id.logoutButton);
        Button resign = findViewById(R.id.resignButton);

        // 스터디 탈퇴
        groupOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("탈퇴 확인");
                builder.setMessage("정말로 스터디를 탈퇴하시겠습니까?");

                builder.setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 "탈퇴" 버튼을 클릭했을 때 실행할 작업
                        // 데이터베이스에서 해당 멤버 삭제
                        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Group/" + groupKey);
                        groupRef.child("members").child(nickname).removeValue();

                        // 다시 개인 그룹 멤버에 추가
                        JoinInfoActivity.addMemberToDefaultGroup(nickname);

                        // 홈화면으로 이동
                        Intent intent = new Intent(MyPageActivity.this, HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 "취소" 버튼을 클릭했을 때 실행할 작업 (아무것도 하지 않음)
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // 로그아웃
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

        // 유저 탈퇴
        // "탈퇴" 버튼 클릭 이벤트 처리
        resign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("탈퇴 확인");
                    builder.setMessage("정말로 탈퇴하시겠습니까?");

                    builder.setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 사용자가 "탈퇴" 버튼을 클릭했을 때 실행할 작업
                            currentUser.delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // 사용자가 성공적으로 삭제된 경우
                                            deleteUserProfileFromDatabase(uid);

                                            // 메인화면으로 이동
                                            Intent intent2 = new Intent(MyPageActivity.this, MainActivity.class);
                                            startActivity(intent2);
                                            overridePendingTransition(0, 0);
                                        } else {
                                            // 사용자 삭제 실패
                                            Exception exception = task.getException();
                                            if (exception != null) {
                                                Log.e("Firebase Delete Error", exception.getMessage());

                                                // Firebase의 예외 처리
                                                if (exception instanceof FirebaseAuthException) {
                                                    FirebaseAuthException firebaseAuthException = (FirebaseAuthException) exception;
                                                    String errorCode = firebaseAuthException.getErrorCode();
                                                    Log.e("Firebase Auth ErrorCode", errorCode);

                                                    // 예외 처리 방법을 errorCode에 따라 분기 처리
                                                    if (errorCode.equals("ERROR_WEAK_PASSWORD")) {
                                                        // 약한 비밀번호로 인한 실패
                                                        // 처리 방법 추가
                                                    } else if (errorCode.equals("다른 에러 코드")) {
                                                        // 다른 실패 원인에 따른 처리
                                                        // 처리 방법 추가
                                                    } else {
                                                        // 기타 실패 원인에 따른 처리
                                                        // 처리 방법 추가
                                                    }
                                                } else {
                                                    // FirebaseAuthException이 아닌 다른 예외의 경우
                                                    // 추가 예외 처리 수행
                                                }
                                            } else {
                                                Log.e("Firebase Delete Error", "Unknown error");
                                            }
                                        }
                                    });
                        }
                    });

                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 사용자가 "취소" 버튼을 클릭했을 때 실행할 작업 (아무것도 하지 않음)
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // 사용자가 로그인되어 있지 않을 때 처리
                    // 예를 들어, 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
                }
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
    private void updateUIWithNickname() {
        helloText.setText(nickname + "님, 반가워요!");
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
                        groupKey = groupSnapshot.getKey();
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
            memberAdapter = new MemberAdapter(memberList);
            memberRecyclerView.setLayoutManager(new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.HORIZONTAL, false));
            memberRecyclerView.setAdapter(memberAdapter);
        }
    }

    private void deleteUserProfileFromDatabase(String userUid) {
        // Firebase Realtime Database 참조 가져오기
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 그룹에서 사용자 제거
        removeUserFromGroups(nickname);

        // 해당 사용자의 정보를 삭제
        usersRef.child(userUid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 사용자 정보 삭제 성공
                    Log.d("test", "탈퇴 완료 및 사용자 정보 삭제 성공");
                })
                .addOnFailureListener(e -> {
                    // 사용자 정보 삭제 실패
                    // 예외 처리 또는 오류 메시지 표시
                    Log.d("test", "사용자 정보 삭제 실패");
                });
    }

    private void removeUserFromGroups(String nickname) {
        // Firebase Realtime Database 참조 가져오기
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    // 그룹의 `members` 노드에서 해당 유저 삭제
                    groupsRef.child(groupSnapshot.getKey()).child("members").child(nickname).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // 사용자를 그룹에서 제거한 경우
                                Log.d("test", "그룹에서 사용자 제거 성공");
                            })
                            .addOnFailureListener(e -> {
                                // 사용자를 그룹에서 제거하지 못한 경우
                                Log.d("test", "그룹에서 사용자 제거 실패");
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the event of the database operation being canceled
                if (databaseError != null) {
                    // Log the error or take appropriate action
                    Log.e("FirebaseError", "Database operation canceled: " + databaseError.getMessage());
                }
            }
        });
    }
}
