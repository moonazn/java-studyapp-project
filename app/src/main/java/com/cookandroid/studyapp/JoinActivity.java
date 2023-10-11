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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class JoinActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mAuth = FirebaseAuth.getInstance();

        final EditText emailEditText = findViewById(R.id.email_area);
        final EditText passwordEditText = findViewById(R.id.password_area);
        Button nextButton = findViewById(R.id.next_button);
        TextView message = findViewById(R.id.message);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // 이메일 또는 비밀번호가 비어 있으면 경고 표시
                    message.setText("이메일과 비밀번호를 모두 입력하세요.");
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(JoinActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // 회원가입 성공
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        Intent intent = new Intent(JoinActivity.this, JoinInfoActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    } else {
                                        // 회원가입 실패
                                        // 예외 처리 또는 오류 메시지 표시
                                        message.setText("회원가입 실패: " + task.getException().getMessage());
                                    }
                                }
                            });
                }
            }
        });


    }
}