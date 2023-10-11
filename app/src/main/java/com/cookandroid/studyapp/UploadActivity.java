package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private ImageView photoImageView;
    private EditText photoTitleEditText;
    private Button okButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        photoImageView = findViewById(R.id.photo);
        photoTitleEditText = findViewById(R.id.photo_title);
        okButton = findViewById(R.id.okButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 이전 화면에서 전달받은 사진을 ImageView에 표시
        Bitmap photoBitmap = getIntent().getParcelableExtra("photo");
        if (photoBitmap != null) {
            photoImageView.setImageBitmap(photoBitmap);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 업로드할 데이터를 가져옵니다.
                String userId = currentUser.getUid();
                String photoTitle = photoTitleEditText.getText().toString();
                Long uploadTime = System.currentTimeMillis(); // 업로드 시간을 현재 시간으로 설정

                // Firebase Storage 레퍼런스 생성
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                // 사진을 업로드할 Storage 경로 생성 (예: photos/userId/photoTitle.jpg)
                String photoPath = "photos/" + userId + "/" + photoTitle + ".jpg";
                StorageReference photoRef = storageRef.child(photoPath);

                // Bitmap 이미지를 ByteArray로 변환
                Bitmap photoBitmap = getIntent().getParcelableExtra("photo");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] photoData = baos.toByteArray();

                // Storage에 사진 업로드
                UploadTask uploadTask = photoRef.putBytes(photoData);
                uploadTask.addOnSuccessListener(UploadActivity.this, taskSnapshot -> {
                    // 업로드 성공 시 다운로드 URL 가져오기
                    photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // 다운로드 URL을 사용하여 Firebase Realtime Database에 데이터 추가
                        String photoUrl = uri.toString();
                        Log.d("photourl : ", photoUrl);
                        UploadData uploadData = new UploadData(photoTitle, userId, uploadTime, photoUrl);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("uploads");
                        DatabaseReference newUploadRef = databaseReference.push(); // 새로운 데이터의 참조를 가져옴
                        newUploadRef.setValue(uploadData.toMap());

                        // 업로드 성공 처리
                        Toast.makeText(UploadActivity.this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();

                        // 업로드 성공 후, BoardActivity로 이동
                        Intent intent = new Intent(UploadActivity.this, BoardActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    });
                }).addOnFailureListener(e -> {
                    // 업로드 실패 처리
                    Toast.makeText(UploadActivity.this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
