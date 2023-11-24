package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.groupKey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<UploadData> uploadDataList;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        uploadDataList = new ArrayList<>();
        postAdapter = new PostAdapter(uploadDataList, this);
        recyclerView.setAdapter(postAdapter);
        recyclerView.scrollToPosition(0);       // 이게 왜 작동이 안되지??????⭐️


        // Firebase Realtime Database에서 데이터를 가져와서 업로드 데이터 리스트에 추가
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group/"+ groupKey + "/uploads");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploadDataList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UploadData uploadData = snapshot.getValue(UploadData.class);
                    if (uploadData != null) {
                        // 업로드 데이터에 있는 유저 UID를 사용하여 유저 닉네임을 가져옵니다.
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        usersRef.child(uploadData.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String userNickname = dataSnapshot.child("nickname").getValue(String.class);
                                    // 유저의 닉네임을 업로드 데이터에 설정합니다.
                                    uploadData.setUser_id(userNickname);
                                    // 업로드 데이터를 리스트에 추가
                                    uploadDataList.add(uploadData);

                                    // 로그에 유저 닉네임과 업로드 데이터 제목을 출력
                                    Log.d("FirebaseData", "User Nickname: " + userNickname + ", Title: " + uploadData.getPhoto_title());

                                    // 데이터 변경 후 스크롤
                                    postAdapter.notifyDataSetChanged();
                                    recyclerView.post(() -> recyclerView.scrollToPosition(0));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 오류 처리
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 가져오기 실패 또는 오류 처리
            }
        });



        Button upload = findViewById(R.id.uploadButton);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 앱을 실행하여 사진 촬영 요청
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        ImageButton rank = findViewById(R.id.rankButton);

        rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, RankActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_exit);
                finish();
            }
        });

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        board.setAlpha(1f);

        // 바텀 바 이동 이벤트
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // 사진을 성공적으로 촬영한 경우, "UPLOAD" 화면으로 이동하고 사진을 전달합니다.
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            uploadIntent.putExtra("photo", imageBitmap);
            startActivity(uploadIntent);
        }
    }
}