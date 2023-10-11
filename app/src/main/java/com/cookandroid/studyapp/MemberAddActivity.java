package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MemberAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memberadd);

        TextView startButton = findViewById(R.id.startButton);

        // 클릭 리스너를 추가
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 전환을 위한 Intent 생성
                Intent intent = new Intent(MemberAddActivity.this, HomeActivity.class);

                // Intent를 사용하여 화면을 전환합니다.
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_exit);
            }
        });
    }
}