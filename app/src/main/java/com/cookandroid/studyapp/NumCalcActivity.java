package com.cookandroid.studyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NumCalcActivity extends AppCompatActivity {
    private TextView questionTextView;
    private EditText resultEditText;
    private Button answerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_num_calc);

        // 레이아웃에서 위젯을 참조합니다.
        questionTextView = findViewById(R.id.questionTextView);
        resultEditText = findViewById(R.id.resultEditText);
        answerButton = findViewById(R.id.answerButton);

        // 질문 텍스트 설정
        questionTextView.setText("Question: 21+13");

        // 정답 확인 버튼 클릭 이벤트 처리
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력된 답을 가져옵니다.
                String answer = resultEditText.getText().toString();

                // 정답을 확인하고 피드백을 제공합니다.
                if (answer.equals("3")) {
                    Toast.makeText(NumCalcActivity.this, "정답입니다!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NumCalcActivity.this, "틀렸습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}