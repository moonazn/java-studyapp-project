package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class WizeSayingActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView photoImageView;
    private TextView sayingTextView;
    private String[] wiseSayings = {
            "삶이 있는 한 희망은 있다.",
            "하루에 3시간을 걸으면 7년 후에 지구를 한 바퀴 돌 수 있다.",
            "언제나 현재에 집중할 수 있다면 행복할 것이다.",
            "너 자신을 알라.",
            "이 또한 곧 지나가리"
            // 여러 명언을 추가할 수 있습니다.
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wize_saying);

        photoImageView = findViewById(R.id.photoImageView);
        Button captureButton = findViewById(R.id.captureButton);

        sayingTextView = findViewById(R.id.sayingTextView);
        Button showSayingButton = findViewById(R.id.showSayingButton);

        showSayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRandomSaying();
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void showRandomSaying() {
        Random random = new Random();
        int index = random.nextInt(wiseSayings.length);

        String randomSaying = wiseSayings[index];
        sayingTextView.setText(randomSaying);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                // 이미지를 ImageView에 표시
                photoImageView.setImageBitmap((Bitmap) extras.get("data"));
            }
        }
    }
}