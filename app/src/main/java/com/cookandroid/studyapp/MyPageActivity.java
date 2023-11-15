package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final int REQUEST_ENABLE_ADMIN = 1;
    private static final int REQUEST_PROVISION_PROFILE = 2;

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();

    static String nickname = ""; // 닉네임을 저장할 변수

    static String groupKey = "";

    int praisePoint = 0;
    private TextView helloText;

    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        helloText = findViewById(R.id.greetText);

        ImageView editImage = findViewById(R.id.editButton);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child("-"+uid);

        // 사용자의 UID를 기반으로 해당 사용자의 닉네임 가져오기
        if (nickname.equals("")) {
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nickname = dataSnapshot.child("nickname").getValue(String.class);
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

        ImageView info = findViewById(R.id.info);
        TextView praisePointTextView = findViewById(R.id.praisePoints);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExplanationPopup();
            }
        });

        // Firebase Realtime Database에서 사용자의 칭찬 점수를 읽어옵니다.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child("-"+uid).child("praisePoints");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot에서 칭찬 점수를 가져와서 설정
                    praisePoint = dataSnapshot.getValue(Integer.class);
                }
                praisePointTextView.setText("나의 칭찬점수 : " + praisePoint);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 에러 처리
            }
        });


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
        TextView removeDevAdmin = findViewById(R.id.removeDevAdmin);

        appLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                        Intent intent = new Intent(MyPageActivity.this, AppLockActivity.class);
//                        startActivity(intent);
                // Check if the timer is running, and if so, start LockInfoActivity
                if (TimerService.isTimerRunning()) {
                    Intent lockInfoIntent = new Intent(MyPageActivity.this, AppLockINGActivity.class);
                    startActivity(lockInfoIntent);
                } else {
                    ComponentName adminComponent = new ComponentName(MyPageActivity.this, MyDeviceAdminReceiver.class);
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    String packageName = getPackageName();

                    boolean isAdminActive = devicePolicyManager.isAdminActive(adminComponent);
                    boolean isProfileOwnerApp = devicePolicyManager.isProfileOwnerApp(packageName);

                    if (isAdminActive && isProfileOwnerApp) {
                        // 디바이스 관리자 권한 및 프로필 관리자 권한 모두 설정된 경우
                        Intent intent = new Intent(MyPageActivity.this, AppLockActivity.class);
                        startActivity(intent);
                    } else {
                        // 권한이 설정되지 않은 경우 또는 하나 이상의 권한이 설정되지 않은 경우
                        if (!isAdminActive) {
                            // 디바이스 관리자 권한이 설정되지 않은 경우
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "디바이스 관리자 권한 설명");
                            startActivityForResult(intent, 1); // 디바이스 관리자 권한 설정 화면 열기
                        }
                        if (!isProfileOwnerApp) {
                            // 프로필 관리자 권한이 설정되지 않은 경우
                            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
                            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent);
                            startActivityForResult(intent, REQUEST_PROVISION_PROFILE); // 프로필 관리자 권한 설정 화면 열기
                        }
                    }
                }
            }
        });

        removeDevAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName adminComponent = new ComponentName(MyPageActivity.this, MyDeviceAdminReceiver.class);

                if (mDevicePolicyManager.isAdminActive(adminComponent)) {
                    mDevicePolicyManager.removeActiveAdmin(adminComponent);
                    String message = "기기 관리자 권한 해제 완료.";
                    Toast.makeText(MyPageActivity.this, message, Toast.LENGTH_SHORT).show();
                }

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
        usersRef.child(uid).removeValue()
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
    private void showExplanationPopup() {
        // LayoutInflater를 사용하여 레이아웃을 가져옴
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.praise_info_popup_layout, null);

        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog alertDialog = builder.create();

        // 팝업 창의 닫기 버튼에 대한 클릭 리스너 설정
        Button closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // 팝업 창 표시
        alertDialog.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // 디바이스 관리자 권한 설정 화면으로부터 결과를 확인
            if (resultCode == RESULT_OK) {
                // 디바이스 관리자 권한이 설정됨
                checkProfileOwnerPermission(); // 프로필 관리자 권한 확인
            } else {
                // 디바이스 관리자 권한 설정이 실패한 경우 또는 사용자가 취소한 경우
                String message = "기기 관리자 권한 승인 실패. 앱 잠금 기능을 실행할 수 없습니다.";
                Toast.makeText(MyPageActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PROVISION_PROFILE) {
            // 프로필 관리자 권한 설정 화면으로부터 결과를 확인
            if (resultCode == RESULT_OK) {
                // 프로필 관리자 권한이 설정됨
                // 모든 권한이 설정되었으므로 AppLockActivity로 이동
                Intent intent = new Intent(MyPageActivity.this, AppLockActivity.class);
                startActivity(intent);
            } else {
                // 프로필 관리자 권한 설정이 실패한 경우 또는 사용자가 취소한 경우
                String message = "프로필 관리자 권한 승인 실패. 앱 잠금 기능을 실행할 수 없습니다.";
                Toast.makeText(MyPageActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkProfileOwnerPermission() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(MyPageActivity.this, MyDeviceAdminReceiver.class);
        String packageName = getPackageName();
        boolean isProfileOwnerApp = devicePolicyManager.isProfileOwnerApp(packageName);

        if (!isProfileOwnerApp) {
            // 프로필 관리자 권한이 설정되지 않은 경우
            // 프로필 관리자 권한 설정 화면 열기
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent);
            startActivityForResult(intent, REQUEST_PROVISION_PROFILE);
        } else {
            // 모든 권한이 설정되었으므로 AppLockActivity로 이동
            Intent intent = new Intent(MyPageActivity.this, AppLockActivity.class);
            startActivity(intent);
        }
    }

}
