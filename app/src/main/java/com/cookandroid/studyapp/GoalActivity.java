package com.cookandroid.studyapp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;

public class GoalActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TextView textViewSelectedDate;
    private Button buttonConfirm;
    private EditText et_memo;
    private ArrayList<String> goalList;
    private ArrayAdapter<String> adapter;
    private TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        datePicker = findViewById(R.id.datePicker);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        et_memo = findViewById(R.id.et_memo);
        textInputLayout = findViewById(R.id.textInputLayout);
        textInputLayout.setVisibility(View.GONE); // 처음에는 숨김

        // 목표 목록을 저장하기 위한 ArrayList 및 어댑터를 초기화
        goalList = new ArrayList<>();  // 목표 목록을 저장하는 ArrayList 초기화
        adapter = new ArrayAdapter<>( // 목표 목록을 표시할 어댑터 초기화
                this,
                android.R.layout.simple_list_item_1, // 간단한 텍스트 뷰를 사용하는 레이아웃
                goalList       // 표시할 목표 목록 데이터
        );


        // 확인 버튼에 대한 클릭 리스너를 설정
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();

                String selectedDate = "선택된 날짜: " + year + "년 " + month + "월 " + day + "일";

                // TextView에 선택된 날짜를 표시
                textViewSelectedDate.setText(selectedDate);

                // TextInputLayout을 보이도록 설정
                textInputLayout.setVisibility(View.VISIBLE);
            }
        });

        // 목표 추가 버튼에 대한 클릭 리스너를 설정
        Button buttonAddGoal = findViewById(R.id.buttonAddGoal);
        buttonAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String memo = et_memo.getText().toString();
                if (!memo.isEmpty()) {
                    goalList.add(memo);
                    adapter.notifyDataSetChanged();
                    et_memo.setText("");
                    textInputLayout.setVisibility(View.GONE); // 팝업 창 닫기
                } else {
                    Toast.makeText(GoalActivity.this, "메모를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // "나의 목표 리스트" 버튼에 대한 클릭 리스너 추가
        Button buttonGoToGoalList = findViewById(R.id.buttonGoToGoal);
        buttonGoToGoalList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GoalActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
    }
}
