package com.cookandroid.studyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileActivity extends AppCompatActivity implements View.OnClickListener {
    private long startTime = 0;
    private Button[] buttons;
    private List<Integer> numbers = new ArrayList<>();
    private int currentNumber = 1;
    private int ButtonCount = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile);

        buttons = new Button[]{
                findViewById(R.id.button1), findViewById(R.id.button2), findViewById(R.id.button3),
                findViewById(R.id.button4), findViewById(R.id.button5), findViewById(R.id.button6),
                findViewById(R.id.button7), findViewById(R.id.button8), findViewById(R.id.button9),
                findViewById(R.id.button10), findViewById(R.id.button11), findViewById(R.id.button12),
                findViewById(R.id.button13), findViewById(R.id.button14), findViewById(R.id.button15),
                findViewById(R.id.button16), findViewById(R.id.button17), findViewById(R.id.button18),
                findViewById(R.id.button19), findViewById(R.id.button20), findViewById(R.id.button21),
                findViewById(R.id.button22), findViewById(R.id.button23), findViewById(R.id.button24),
                findViewById(R.id.button25)
        };

        for (int i = 1; i <= 25; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        for (int i = 0; i < 25; i++) {
            buttons[i].setText(numbers.get(i).toString());
            buttons[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        int number = Integer.parseInt(button.getText().toString());

        if (number == 1) {
            startTimer();
        }

        if (number == currentNumber) {
            button.setVisibility(View.INVISIBLE);
            currentNumber++;
            ButtonCount--;
        }
        if (ButtonCount == 0) {
            showSuccessDialog();
            stopTimer();
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = elapsedTime / 1000;

        dialogBuilder
                .setNegativeButton("끝내기", (dialog, which) -> {
                    AlarmReceiver.stopAlarmSound();
                    finish(); // 액티비티 종료
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private void stopTimer() {
        // Implement if needed
    }

    private void resetGame() {
        currentNumber = 1;
        ButtonCount = 25;

        Collections.shuffle(numbers);

        for (int i = 0; i < 25; i++) {
            buttons[i].setText(numbers.get(i).toString());
            buttons[i].setVisibility(View.VISIBLE);
        }
    }
}