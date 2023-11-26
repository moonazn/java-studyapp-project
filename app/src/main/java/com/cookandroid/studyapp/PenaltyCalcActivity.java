package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;
import static com.cookandroid.studyapp.MyPageActivity.nickname;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PenaltyCalcActivity extends AppCompatActivity {

    private PenaltyMemberAdapter memberAdapter;
    int weeklyPenaltySum;
    private TextView penaltyConditionTextView;
    private String selectedCertificationCondition;  // 선택한 공부 인증 조건을 저장할 변수
    private int selectedStudyTimeCondition;         // 선택한 공부 시간 조건을 저장할 변수
    private String selectedPenaltyAmount;           // 선택한 벌금을 저장할 변수

    private String penaltyCondition;    // 벌금 책정 조건
    private int penaltyConditionValue;  // 인증 건수 또는 공부 시간 조건값
    private int penaltyAmount;          // 건수 당 벌금

    List<MemberWithPenalty> memberList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penalty_calc);

        penaltyConditionTextView = findViewById(R.id.penaltyCondition);
        getGroupKeyForUser(nickname);

// getPenaltyConditionFromFirebase 호출부분
        getPenaltyConditionFromFirebase(new OnPenaltyDataLoadedListener() {
            @Override
            public void onPenaltyDataLoaded() {
                // 여기에 해당 부분이 실행됩니다.
            }
        });

        ImageButton back = findViewById(R.id.backButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 전환을 위한 Intent 생성
                Intent intent = new Intent(PenaltyCalcActivity.this, HomeActivity.class);

                // Intent를 사용하여 화면을 전환합니다.
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_exit);
                finish();
            }
        });

        TextView editConditionTextView = findViewById(R.id.editCondition);
        editConditionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConditionOptionsDialog();
            }
        });

//        // Firebase에서 벌금 조건을 가져오고, 가져온 후에 계산을 시작합니다.
//        getPenaltyConditionFromFirebase();
    }

    private void showConditionOptionsDialog() {
        final CharSequence[] options = {"공부 인증 미달 시", "공부 시간 미달 시"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("수정할 조건을 선택하세요");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which].toString();
                if ("공부 인증 미달 시".equals(selectedOption)) {
                    penaltyCondition = "upload";
                    showStudyCertificationDialog();
                } else if ("공부 시간 미달 시".equals(selectedOption)) {
                    penaltyCondition = "study time";
                    showStudyTimeDialog();
                }
            }
        });

        builder.show();
    }

    private void showStudyCertificationDialog() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("공부 인증 미달 시 조건 선택");
        builder.setView(numberPicker);
        builder.setPositiveButton("다음", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                penaltyConditionValue = numberPicker.getValue();
                handleSelectedStudyCertificationOption(penaltyConditionValue);
                showPenaltyAmountDialog(); // 다음 단계로 이동
            }
        });

        builder.show();
    }

    private void showStudyTimeDialog() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("공부 시간 미달 시 조건 선택");
        builder.setView(numberPicker);
        builder.setPositiveButton("다음", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                penaltyConditionValue = numberPicker.getValue();
                handleSelectedStudyTimeOption(penaltyConditionValue);
                showPenaltyAmountDialog(); // 다음 단계로 이동
            }
        });

        builder.show();
    }

    private void showPenaltyAmountDialog() {
        final CharSequence[] options = {"500원", "1000원", "1500원", "2000원"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("벌금을 선택하세요");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which].toString();

                switch (selectedOption) {
                    case "500원":
                        penaltyAmount = 500;
                        break;
                    case "1000원":
                        penaltyAmount = 1000;
                        break;
                    case "1500원":
                        penaltyAmount = 1500;
                        break;
                    case "2000원":
                        penaltyAmount = 2000;
                        break;
                }
                handleSelectedPenaltyAmount(selectedOption);
                // Firebase에 벌금 조건 저장
                savePenaltyConditionToFirebase(penaltyCondition, penaltyConditionValue, penaltyAmount);

                // 여기에서 최종 결과를 보여주는 메서드를 호출
                showFinalResultToUser();
// getPenaltyConditionFromFirebase 호출부분
                getPenaltyConditionFromFirebase(new OnPenaltyDataLoadedListener() {
                    @Override
                    public void onPenaltyDataLoaded() {
                        // 여기에 해당 부분이 실행됩니다.
                    }
                });
            }
        });

        builder.show();
    }

    private void showFinalResultToUser() {
        // 최종 결과를 사용자에게 보여주는 작업 수행
        updatePenaltyConditionOnUserScreen();

        // 여기에서 최종 결과를 보여주는 메서드를 호출
        if ("공부 시간 미달 시".equals(selectedStudyTimeCondition)) {
            showTimeBasedResultToUser();
        } else if ("공부 인증 미달 시".equals(selectedCertificationCondition)) {
            showCertificationBasedResultToUser();
        }

    }

    // Firebase에 벌금 조건 저장 메서드
    private void savePenaltyConditionToFirebase(String certificationCondition, int studyTimeCondition, int penaltyAmount) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Group").child(groupKey);

        // 그룹 노드에 벌금 조건 정보 저장
        groupRef.child("penalty").child("penaltyCondition").setValue(certificationCondition);
        groupRef.child("penalty").child("penaltyConditionValue").setValue(studyTimeCondition);
        groupRef.child("penalty").child("penaltyAmount").setValue(penaltyAmount);

//        // 새로운 벌금 조건을 기반으로 각 멤버의 벌금을 계산
//        List<MemberWithPenalty> updatedMemberList = calculatePenaltyAmounts();
//
//        // 어댑터를 업데이트
//        memberAdapter.updateData(updatedMemberList);
        reStartActivity();

    }

    // 새로운 조건에 따라 각 멤버의 벌금을 계산하는 메서드
    private void calculatePenaltyAmounts() {

        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(nickname)) {
                        Log.d("test", "groupKey : " + groupKey);
                        memberList = new ArrayList<>();
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            String memberName = memberSnapshot.getKey();

                            MemberWithPenalty member = new MemberWithPenalty(memberName, 0);
                            memberList.add(member);
                        }

                        MemberWithPenalty member;

                        // 멤버를 순회하면서 벌금을 계산
                        for (int i = 0; i < memberList.size(); i++) {
                            member = memberList.get(i);

                            // 멤버의 새로운 조건에 기반한 벌금 계산
                            MemberWithPenalty finalMember = member;
                            int finalI = i;
                            calculatePenaltyForMember(member.getMemberName(), penaltyConditionValue, new OnPenaltyCalculationCompleteListener() {
                                @Override
                                public void onPenaltyCalculationComplete(int weeklyPenaltySum) {

                                    // MemberWithPenalty 객체를 생성하고 리스트에 추가
                                    finalMember.setPenaltyAmount(weeklyPenaltySum);
                                    memberList.set(finalI, finalMember);

                                }
                            });
                            Log.d("updatedMemberList", memberList.get(finalI).getMemberName());
                            Log.d("updatedMemberList", String.valueOf(memberList.get(finalI).getPenaltyAmount()));

                        }

                        if (memberAdapter == null) {
                            initRecyclerView(memberList);
                        } else {
                            memberAdapter.updateData(memberList);
                            memberAdapter.notifyDataSetChanged();
                        }
                    }
                }

//                initRecyclerView(memberList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        initRecyclerView(updatedMemberList);
    }

    private void calculatePenaltyForMember(String memberName, int certificationCondition, OnPenaltyCalculationCompleteListener listener) {
        switch (penaltyCondition){
            case "upload":
                DatabaseReference uploadsRef = FirebaseDatabase.getInstance().getReference("Group").child(groupKey).child("uploads");

                // 사용자 설정 벌금을 일주일 동안의 벌금에 누적할 변수
                int[] weeklyPenaltySum = {0};
                weeklyPenaltySum[0] = 0;

                String[] uid = {""};

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                usersRef.orderByChild("nickname").equalTo(memberName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            uid[0] = snapshot.getKey();
                            Log.d("uid", uid[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                // 특정 멤버의 업로드된 게시물 조회
                uploadsRef.orderByChild("user_id").equalTo(uid[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int uploadCount = (int) dataSnapshot.getChildrenCount();

                        // 최근 일주일 동안의 하루당 업로드된 게시물 수 확인
                        int dailyUploadCount = 0;

                        for (int i = 7; i > 0; i--) {
                            // 최근 일주일 동안의 시작 날짜를 구합니다.
                            long oneWeekAgoTimestamp = getOneWeekAgoTimestamp(i);

                            Log.d("uploadSnapshot", String.valueOf(dataSnapshot.getChildrenCount()));


                            // TODO: 여기가 안됨
                            for (DataSnapshot uploadSnapshot : dataSnapshot.getChildren()) {

                                UploadData upload = uploadSnapshot.getValue(UploadData.class);

                                Long uploadTime = upload.getUpload_time();
                                // uploadTime을 Calendar 객체로 변환
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(uploadTime);

                                if (upload != null && uploadTime >= oneWeekAgoTimestamp) {
                                    dailyUploadCount++;
                                }
                                Log.d("timeStamp", String.valueOf(oneWeekAgoTimestamp) + " vs " + String.valueOf(uploadTime));
                            }

                            weeklyPenaltySum[0] += calculatePenaltyAmountFromUploadCount(dailyUploadCount);
                            Log.d("penalty process", String.valueOf(weeklyPenaltySum[0]));
                            Log.d("dailyUploadCount", String.valueOf(dailyUploadCount));

//                    // 공부 인증 조건과 비교
//                    if (dailyUploadCount < certificationCondition) {
//                        // 조건을 만족하지 않으면 벌금을 책정
//                        // 주간 벌금 합산에 누적
//
//                    }

                        }

                        // weeklyPenaltySum을 memberList에 해당 사용자의 벌금 값으로 설정
                        setWeeklyPenaltySumForMember(memberName, weeklyPenaltySum[0]);
                        Log.d("WeeklyPenaltySum", String.valueOf(weeklyPenaltySum[0]));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 오류 처리
                    }
                });

                listener.onPenaltyCalculationComplete(weeklyPenaltySum[0]);
                Log.d("listener called ", "true");

                break;

            case "study time":
                weeklyPenaltySum = new int[]{0};
                weeklyPenaltySum[0] = 0;

                uid = new String[]{""};

                usersRef = FirebaseDatabase.getInstance().getReference("users");

                usersRef.orderByChild("nickname").equalTo(memberName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            uid[0] = snapshot.getKey();
                            Log.d("uid", uid[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                DatabaseReference timeRef = usersRef.child(uid[0]).child("totalTime");

                timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (int i = 7; i > 0; i--) {
                            // 최근 일주일 동안의 시작 날짜를 구합니다.
                            long oneWeekAgoTimestamp = getOneWeekAgoTimestamp(i);

                            // Timestamp를 Date로 변환
                            Date oneWeekAgoDate = new Date(oneWeekAgoTimestamp);

                            // Date를 원하는 형식의 문자열로 변환
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = dateFormat.format(oneWeekAgoDate);
                            Log.d("WeeklyPenaltySum", "Start of inner loop for date: " + formattedDate);

                            int finalI = i;

                            String totalDurationString = snapshot.child(formattedDate).child("totalDuration").getValue(String.class);
                            Log.d("WeeklyPenaltySum", "Total duration string: " + totalDurationString);
                            int totalDuration;

                            if (totalDurationString == null){
                                totalDuration = 0;
                            } else {
                                totalDuration = parseDurationInSeconds(totalDurationString);
                            }

                            // 해당 날짜의 공부 시간이 기준 시간 미만인 경우 벌금 누적
                            if (totalDuration < (penaltyConditionValue * 60 * 60)) {
                                weeklyPenaltySum[0] += penaltyAmount;
                            }


                            if (finalI == 1) {

                                Log.d("WeeklyPenaltySum", String.valueOf(weeklyPenaltySum[0]));

                                setWeeklyPenaltySumForMember(memberName, weeklyPenaltySum[0]);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                listener.onPenaltyCalculationComplete(weeklyPenaltySum[0]);
                Log.d("listener called ", "true");
                break;
        }


    }

    private int parseDurationInSeconds(String durationAsString) {
        String[] timeUnits = durationAsString.split("\\s+");

        int hours = 0, minutes = 0, seconds = 0;

        for (String unit : timeUnits) {
            if (unit.endsWith("h")) {
                hours = Integer.parseInt(unit.replace("h", ""));
            } else if (unit.endsWith("m")) {
                minutes = Integer.parseInt(unit.replace("m", ""));
            } else if (unit.endsWith("s")) {
                seconds = Integer.parseInt(unit.replace("s", ""));
            }
        }

        // 시, 분, 초를 초 단위로 변환하여 반환
        return hours * 3600 + minutes * 60 + seconds;
    }


    // 특정 멤버의 주간 벌금 합산을 설정하는 메서드
    private void setWeeklyPenaltySumForMember(String memberName, int weeklyPenaltySum) {
        // memberList에서 해당 사용자를 찾아서 주간 벌금 합산을 설정
        for (MemberWithPenalty member : memberList) {
            if (memberName.equals(member.getMemberName())) {
                member.setPenaltyAmount(weeklyPenaltySum);
                break;
            }
        }
    }

    // 최근 일주일 동안의 시작 날짜를 반환하는 메서드
    private long getOneWeekAgoTimestamp(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -i); // 현재 날짜에서 i일 전
        return calendar.getTimeInMillis();
    }

    // 하루당 공부 인증 건수에 따라 벌금을 계산하는 메서드
    private int calculatePenaltyAmountFromUploadCount(int dailyUploadCount) {
        // 벌금 조건에 따라 적절한 로직을 구현하여 벌금을 반환
        // 예시로 하루당 2건 이상이어야 조건을 만족하는 경우 벌금을 0으로 설정합니다.
        if (dailyUploadCount >= penaltyConditionValue) {
            return 0; // 조건을 만족하면 벌금 없음
        } else {
            // 조건을 만족하지 못하면 적절한 벌금을 반환
            return penaltyAmount;
        }
    }

    // 최종 결과를 보여주는 메서드 - 시간에 따른 결과
    private void showTimeBasedResultToUser() {
        // 여기에서 AlertDialog를 사용하여 사용자에게 시간에 따른 최종 결과를 보여줍니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("시간에 따른 최종 결과");
        builder.setMessage("선택한 시간: " + selectedStudyTimeCondition +
                "\n선택한 벌금: " + selectedPenaltyAmount);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 다이얼로그를 닫거나 추가적인 작업을 수행할 수 있습니다.
            }
        });
        builder.show();
    }

    // 최종 결과를 보여주는 메서드 - 인증 건수에 따른 결과
    private void showCertificationBasedResultToUser() {
        // 여기에서 AlertDialog를 사용하여 사용자에게 인증 건수에 따른 최종 결과를 보여줍니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("인증 건수에 따른 최종 결과");
        builder.setMessage("선택한 인증 건수: " + selectedCertificationCondition +
                "\n선택한 벌금: " + selectedPenaltyAmount);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 다이얼로그를 닫거나 추가적인 작업을 수행할 수 있습니다.
            }
        });
        builder.show();
    }

    private void getPenaltyConditionFromFirebase(OnPenaltyDataLoadedListener listener) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Group").child(groupKey).child("penalty");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    penaltyCondition = dataSnapshot.child("penaltyCondition").getValue(String.class);
                    penaltyConditionValue = dataSnapshot.child("penaltyConditionValue").getValue(Integer.class);
                    penaltyAmount = dataSnapshot.child("penaltyAmount").getValue(Integer.class);

                    // 벌금 조건이 존재하면 텍스트뷰에 표시
                    updatePenaltyConditionOnUserScreen();

                    // 벌금 조건을 가져온 후에 계산을 시작합니다.
                    calculatePenaltyAmounts();

                    // 데이터를 가져왔으므로 콜백 호출
                    listener.onPenaltyDataLoaded();
                } else {
                    // 벌금 조건이 존재하지 않으면 기본값 표시
                    getFormattedPenaltyCondition();

                    // 데이터를 가져왔으므로 콜백 호출
                    listener.onPenaltyDataLoaded();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                // 데이터를 가져왔으므로 콜백 호출
                listener.onPenaltyDataLoaded();
            }
        });
    }

    void reStartActivity(){
        // Firebase 데이터를 비동기적으로 가져오기
        getPenaltyConditionFromFirebase(new OnPenaltyDataLoadedListener() {
            @Override
            public void onPenaltyDataLoaded() {
                // Firebase 데이터를 가져온 후에 화면을 업데이트
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });
    }
    // Firebase 데이터를 비동기적으로 가져오기 위한 인터페이스
    public interface OnPenaltyDataLoadedListener {
        void onPenaltyDataLoaded();
    }


    private void updatePenaltyConditionTextView() {
        if (penaltyConditionTextView != null) {
            penaltyConditionTextView.setText("벌금 조건 : \n" + selectedCertificationCondition);
        }
    }

    private void updatePenaltyConditionOnUserScreen() {
        int penaltyConditionTextViewId = R.id.penaltyCondition;
        TextView userScreenTextView = findViewById(penaltyConditionTextViewId);
        if (userScreenTextView != null) {
            userScreenTextView.setText(getFormattedPenaltyCondition());
        }
    }
    private String getFormattedPenaltyCondition() {
        // 벌금 조건을 적절히 포맷팅하여 반환하는 메서드
        String formattedCondition;
        if ("upload".equals(penaltyCondition)) {
            formattedCondition = "매일 공부인증 " + penaltyConditionValue + "건 이상 하지 않을 시 => " + penaltyAmount + "원";
        } else if ("study time".equals(penaltyCondition)) {
            formattedCondition = "공부시간 " + penaltyConditionValue + "시간 미만일 시 => " + penaltyAmount + "원";
        } else {
            formattedCondition = "벌금 조건 설정 안함";
        }
        return formattedCondition;
    }

    private void handleSelectedPenaltyAmount(String selectedOption) {
        Toast.makeText(PenaltyCalcActivity.this, "선택한 벌금: " + selectedOption, Toast.LENGTH_SHORT).show();
        updatePenaltyConditionOnUserScreen();
    }

    private void handleSelectedStudyCertificationOption(int selectedNumber) {
        selectedCertificationCondition = "매일 공부인증 " + selectedNumber + "건 이상 하지 않을 시 => ";
        updatePenaltyConditionTextView();
        Toast.makeText(PenaltyCalcActivity.this, "공부 인증 미달 시 조건 선택: " + selectedNumber, Toast.LENGTH_SHORT).show();
    }

    private void handleSelectedStudyTimeOption(int selectedNumber) {
        selectedCertificationCondition = "공부시간 " + selectedNumber + "시간 미만일 시 => ";
        updatePenaltyConditionTextView();
        Toast.makeText(PenaltyCalcActivity.this, "공부 시간 미달 시 조건 선택: " + selectedNumber, Toast.LENGTH_SHORT).show();
    }

    private void getGroupKeyForUser(String currentUserNickname) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(nickname)) {
                        Log.d("test", "groupKey : " + groupKey);
                        memberList = new ArrayList<>();
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            String memberName = memberSnapshot.getKey();

                            MemberWithPenalty member = new MemberWithPenalty(memberName, 0);
                            memberList.add(member);
                        }

                        MemberWithPenalty member;

                        // 멤버를 순회하면서 벌금을 계산
                        for (int i = 0; i < memberList.size(); i++) {
                            member = memberList.get(i);

                            // 멤버의 새로운 조건에 기반한 벌금 계산
                            MemberWithPenalty finalMember = member;
                            int finalI = i;
                            calculatePenaltyForMember(member.getMemberName(), penaltyConditionValue, new OnPenaltyCalculationCompleteListener() {
                                @Override
                                public void onPenaltyCalculationComplete(int weeklyPenaltySum) {

                                    // MemberWithPenalty 객체를 생성하고 리스트에 추가
                                    finalMember.setPenaltyAmount(weeklyPenaltySum);
                                    memberList.set(finalI, finalMember);

                                    Log.d("updatedMemberList", memberList.get(finalI).getMemberName());
                                    Log.d("updatedMemberList", String.valueOf(memberList.get(finalI).getPenaltyAmount()));

                                }
                            });
                        }
                        if (memberAdapter == null) {
                            initRecyclerView(memberList);
                        } else {
                            memberAdapter.updateData(memberList);
                            memberAdapter.notifyDataSetChanged();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public interface OnPenaltyCalculationCompleteListener {
        void onPenaltyCalculationComplete(int weeklyPenaltySum);
    }


    private void initRecyclerView(List<MemberWithPenalty> memberList) {
        RecyclerView memberRecyclerView = findViewById(R.id.memberRecyclerView);

        if (memberList.isEmpty()) {
            Toast.makeText(this, "멤버가 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            memberAdapter = new PenaltyMemberAdapter(memberList);
            memberRecyclerView.setLayoutManager(new LinearLayoutManager(PenaltyCalcActivity.this, LinearLayoutManager.VERTICAL, false));
            memberRecyclerView.setAdapter(memberAdapter);
        }
    }
}