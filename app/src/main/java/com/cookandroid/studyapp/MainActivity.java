package com.cookandroid.studyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // 사용자가 로그인한 경우, Home 화면으로 이동
            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }

        final EditText emailEditText = findViewById(R.id.email_area);
        final EditText passwordEditText = findViewById(R.id.password_area);
        TextView message = findViewById(R.id.message);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // 이메일 또는 비밀번호가 비어 있으면 경고 표시
                    message.setText("이메일과 비밀번호를 모두 입력하세요.");
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // 로그인 성공
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 로그인 실패
                                        Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    // 화면 전환을 위한 Intent 생성
                    Intent intent = new Intent(MainActivity.this, MemberAddActivity.class);

                    // Intent를 사용하여 화면을 전환합니다.
                    startActivity(intent);

                }
            }
        });

        TextView joinButton = findViewById(R.id.join_button);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 전환을 위한 Intent 생성
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);

                // Intent를 사용하여 화면을 전환합니다.
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

}