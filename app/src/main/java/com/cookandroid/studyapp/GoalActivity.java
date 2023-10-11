package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class GoalActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TextView textViewSelectedDate;
    private Button buttonConfirm;
    private EditText et_memo;
    private ListView listViewGoals;
    private ArrayList<String> goalList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);


        datePicker = findViewById(R.id.datePicker);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        et_memo = findViewById(R.id.et_memo);

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
                int month = datePicker.getMonth() + 1; int day = datePicker.getDayOfMonth();


                String selectedDate = "선택된 날짜: " + year + "년 " + month + "월 " + day + "일";

                // TextView에 선택된 날짜를 표시
                textViewSelectedDate.setText(selectedDate);
            }
        });
    }
}
