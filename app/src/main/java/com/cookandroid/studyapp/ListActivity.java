package com.cookandroid.studyapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
public class ListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 이 액티비티에서 할 일 목록 및 기타 기능을 구현

        // 뒤로 가기 버튼을 눌렀을 때 홈 화면으로 돌아가도록 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 뒤로 가기 버튼을 누를 때 현재 액티비티 종료
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // 뒤로 가기 버튼을 누를 때 현재 액티비티 종료
    }
}
