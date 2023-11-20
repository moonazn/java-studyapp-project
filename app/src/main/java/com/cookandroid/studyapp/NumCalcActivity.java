package com.cookandroid.studyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class NumCalcActivity extends AppCompatActivity {
    private TextView questionTextView;
    private EditText resultEditText;
    private Button answerButton;

    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_num_calc);

        questionTextView = findViewById(R.id.questionTextView);
        resultEditText = findViewById(R.id.resultEditText);
        answerButton = findViewById(R.id.answerButton);

        generateRandomQuestion();

        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = resultEditText.getText().toString();
                checkAnswer(answer);
            }
        });
    }

    private void generateRandomQuestion() {
        int num1 = random.nextInt(100);
        int num2 = random.nextInt(100);
        char operator = getRandomOperator();

        // Ensure that subtraction doesn't result in a negative number
        if (operator == '-' && num1 < num2) {
            // If subtraction would result in a negative number, swap the numbers
            int temp = num1;
            num1 = num2;
            num2 = temp;
        }

        // Ensure that division doesn't result in a non-integer answer
        if (operator == '/' && num1 % num2 != 0) {
            // If division doesn't result in a whole number, regenerate the numbers
            generateRandomQuestion();
        } else {
            questionTextView.setText("문제: " + num1 + " " + operator + " " + num2);
        }
    }

    private int calculateAnswer(int num1, int num2, char operator) {
        switch (operator) {
            case '+':
                return num1 + num2;
            case '-':
                return num1 - num2;
            case 'x':
                return num1 * num2;
            case '/':
                return num1 / num2;
            default:
                throw new IllegalArgumentException("Unknown operator");
        }
    }

    private char getRandomOperator() {
        char[] operators = {'+', '-', 'x', '/'};
        return operators[random.nextInt(operators.length)];
    }

    private void checkAnswer(String userAnswer) {
        String questionText = questionTextView.getText().toString().replace("문제: ", "");
        String[] parts = questionText.split(" ");
        int num1 = Integer.parseInt(parts[0]);
        char operator = parts[1].charAt(0);
        int num2 = Integer.parseInt(parts[2]);

        int correctAnswer = calculateAnswer(num1, num2, operator);

        if (userAnswer.equals(String.valueOf(correctAnswer)) && correctAnswer >= 0) {
            Toast.makeText(NumCalcActivity.this, "정답입니다!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(NumCalcActivity.this, "틀렸습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
        }

        generateRandomQuestion();
    }
}