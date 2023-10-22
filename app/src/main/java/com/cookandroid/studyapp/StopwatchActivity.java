package com.cookandroid.studyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StopwatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        Button addStopwatchButton = findViewById(R.id.addStopwatchButton);

        addStopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 스톱워치 화면으로 이동
                Intent intent = new Intent(StopwatchActivity.this, AddStopwatchActivity.class);
                startActivity(intent);
            }
        });
    }
}
