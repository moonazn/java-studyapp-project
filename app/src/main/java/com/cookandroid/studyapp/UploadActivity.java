package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ImageView photoImageView = findViewById(R.id.photo);

        // 이전 화면에서 전달받은 사진을 ImageView에 표시
        Bitmap photoBitmap = getIntent().getParcelableExtra("photo");
        if (photoBitmap != null) {
            photoImageView.setImageBitmap(photoBitmap);
        }

        Button ok = findViewById(R.id.okButton);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}