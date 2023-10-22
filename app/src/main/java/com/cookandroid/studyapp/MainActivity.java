package com.cookandroid.studyapp;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;
    final String webClientID = "297290158627-9kbe5sr86njkijr55hsnb6ljqjb0ua76.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // 사용자가 로그인한 경우, Home 화면으로 이동
            Intent homeIntent = new Intent(this, MyPageActivity.class);
            startActivity(homeIntent);
        }

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientID) // Replace with your web client ID
                .requestEmail()
                .build();

        // Build the GoogleSignInClient with the options
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

                                        // 화면 전환을 위한 Intent 생성
                                        Intent intent = new Intent(MainActivity.this, MemberAddActivity.class);

                                        // Intent를 사용하여 화면을 전환합니다.
                                        startActivity(intent);
                                    } else {
                                        // 로그인 실패
                                        Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();

                                        message.setText("유효하지 않은 로그인 정보입니다.");
                                    }
                                }
                            });


                }
            }
        });

        ImageView googleLoginButton = findViewById(R.id.googleLogin);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 구글 로그인 창 열기
                signInWithGoogle();
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

    // signInWithGoogle() 메서드 내에서 처리
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // 이미 Firebase에 등록된 이메일 주소인지 확인
                String email = account.getEmail();
                FirebaseUser existingUser = FirebaseAuth.getInstance().getCurrentUser();

                Log.d(TAG, "onActivityResult: " + email);

                if (existingUser != null && existingUser.getEmail().equals(email)) {
                    // 이미 Firebase에 등록된 사용자인 경우, 연동
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    existingUser.linkWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // 이미 Firebase에 등록된 사용자와 연동 성공
                                        // 추가 정보 수집 등의 처리 수행
                                        Log.d("test", "1");
                                        Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // 연동 실패 처리
                                        Log.d("test", "2");
                                    }
                                }
                            });
                } else {

                    // Firebase에 등록되지 않은 사용자인 경우, 회원가입 진행
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        // 로그인 성공 후 처리
                                        if (user != null) {
                                            // 사용자 정보를 추가로 입력받을 수 있는 화면으로 이동
                                            // 예: JoinInfoActivity
                                            Log.d("test", "3");
                                            Intent intent = new Intent(MainActivity.this, JoinInfoActivity.class);
                                            startActivity(intent);

                                        }
                                    } else {
                                        // 회원가입 실패 처리
                                        Log.d("test", "4");
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                // 구글 로그인 실패 처리
                Log.d("test", "Google 로그인 실패, 코드: " + e.getStatusCode());
            }

        }
    }

}
