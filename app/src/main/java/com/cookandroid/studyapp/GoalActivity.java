package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GoalActivity extends AppCompatActivity {
    private EditText etGoalMemo;
    private ListView listViewGoals;
    private ArrayList<String> goalList;
    private ArrayAdapter<String> goalAdapter;
    private Button buttonAddGoal;
    private int maxMemoLength = 20;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        etGoalMemo = findViewById(R.id.etGoalMemo);
        listViewGoals = findViewById(R.id.listViewGoals);
        buttonAddGoal = findViewById(R.id.buttonAddGoal);

        goalList = new ArrayList<>();
        goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goalList);
        listViewGoals.setAdapter(goalAdapter);

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("goals");

        etGoalMemo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > maxMemoLength) {
                    etGoalMemo.setText(charSequence.subSequence(0, maxMemoLength));
                    etGoalMemo.setSelection(maxMemoLength);
                    Toast.makeText(GoalActivity.this, "최대 " + maxMemoLength + "글자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        buttonAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String memo = etGoalMemo.getText().toString().trim();
                if (!memo.isEmpty()) {
                    // Firebase Realtime Database에 목표 추가
                    String goalId = databaseReference.push().getKey();
                    databaseReference.child(goalId).setValue(memo);
                    goalList.add(memo); // 목표 목록에도 추가
                    goalAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알림

                    etGoalMemo.setText("");
                } else {
                    Toast.makeText(GoalActivity.this, "목표를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}