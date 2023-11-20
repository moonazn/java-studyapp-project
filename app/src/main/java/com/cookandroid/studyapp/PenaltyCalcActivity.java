package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;
import static com.cookandroid.studyapp.MyPageActivity.nickname;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class PenaltyCalcActivity extends AppCompatActivity {

    private PenaltyMemberAdapter memberAdapter;
    private TextView penaltyConditionTextView;
    private String selectedCertificationCondition;  // 선택한 공부 인증 조건을 저장할 변수
    private int selectedStudyTimeCondition;         // 선택한 공부 시간 조건을 저장할 변수
    private String selectedPenaltyAmount;           // 선택한 벌금을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penalty_calc);

        penaltyConditionTextView = findViewById(R.id.penaltyCondition);
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

        TextView editConditionTextView = findViewById(R.id.editCondition);
        editConditionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConditionOptionsDialog();
            }
        });
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
                    showStudyCertificationDialog();
                } else if ("공부 시간 미달 시".equals(selectedOption)) {
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
                int selectedNumber = numberPicker.getValue();
                handleSelectedStudyCertificationOption(selectedNumber);
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
                int selectedNumber = numberPicker.getValue();
                handleSelectedStudyTimeOption(selectedNumber);
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
                handleSelectedPenaltyAmount(selectedOption);

                // 여기에서 최종 결과를 보여주는 메서드를 호출
                showFinalResultToUser();
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


    private void updatePenaltyConditionTextView() {
        if (penaltyConditionTextView != null) {
            penaltyConditionTextView.setText("벌금 조건 : \n" + selectedCertificationCondition);
        }
    }

    private void updatePenaltyConditionOnUserScreen() {
        int penaltyConditionTextViewId = R.id.penaltyCondition;
        TextView userScreenTextView = findViewById(penaltyConditionTextViewId);
        if (userScreenTextView != null) {
            userScreenTextView.setText(selectedCertificationCondition + "\n" + selectedPenaltyAmount);
        }
    }

    private void handleSelectedPenaltyAmount(String selectedOption) {
        Toast.makeText(PenaltyCalcActivity.this, "선택한 벌금: " + selectedOption, Toast.LENGTH_SHORT).show();
        selectedPenaltyAmount = selectedOption;
        updatePenaltyConditionOnUserScreen();
    }

    private void handleSelectedStudyCertificationOption(int selectedNumber) {
        selectedCertificationCondition = "매일 공부인증 " + selectedNumber + "건 이상 하지 않을 시 => ";
        updatePenaltyConditionTextView();
        Toast.makeText(PenaltyCalcActivity.this, "공부 인증 미달 시 조건 선택: " + selectedNumber, Toast.LENGTH_SHORT).show();
    }

    private void handleSelectedStudyTimeOption(int selectedNumber) {
        selectedStudyTimeCondition = selectedNumber;
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

                    if (membersSnapshot.hasChild(currentUserNickname)) {
                        Log.d("test", "groupKey : " + groupKey);
                        List<String> memberList = new ArrayList<>();
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            String memberName = memberSnapshot.getKey();
                            memberList.add(memberName);
                        }
                        initRecyclerView(memberList);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    private void initRecyclerView(List<String> memberList) {
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