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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;



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

        // Google Sign-In 버튼 처리
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientID)
                .requestEmail()
                .build();

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

//        googleLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//            }
//        });

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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                Log.w(TAG, "Google sign in failed", e);
//                // 구글 로그인 실패 처리
//            }
//        }
//    }

//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
//
//                            if (checkUserInBackground(user)) {
//                                // 새로운 사용자인 경우, Firebase에 회원가입
//                                // 사용자의 이메일 주소를 Firebase Authentication에 등록
//                                String email = user.getEmail();
//                                String password = "SomeSecurePassword"; // 임시 비밀번호
//                                mAuth.createUserWithEmailAndPassword(email, password)
//                                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                                if (task.isSuccessful()) {
//                                                    // Firebase에 성공적으로 회원가입한 경우, JoinInfoActivity로 이동
//                                                    Log.w(TAG, "회원가입 성공, 이제 닉네임 입력");
//                                                    Intent intent = new Intent(MainActivity.this, JoinInfoActivity.class);
//                                                    startActivity(intent);
//                                                } else {
//                                                    // 회원가입 실패 처리
//                                                    Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
//                                                    Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                            } else {
//                                // 이미 가입한 사용자인 경우, MyPageActivity로 이동
//                                Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
//                                startActivity(intent);
//                            }
//                        } else {
//                            // Firebase 로그인 실패 처리
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }

//    private boolean isNewUser(FirebaseUser user) {
//        boolean isNew = true; // 기본적으로 새로운 사용자로 설정
//
//        if (user == null) {
//            // 사용자가 로그인하지 않았거나 Firebase에 로그인한 사용자 정보가 없는 경우
//            Log.d("test", "사용자가 로그인하지 않았거나 Firebase에 로그인한 사용자 정보가 없는 경우");
//        } else {
//            // 사용자가 로그인한 경우, Firebase Realtime Database에서 사용자 정보를 확인
//            String userUid = user.getUid();
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
//
//            // 사용자 정보를 Realtime Database에서 가져오기
//            DataSnapshot dataSnapshot = databaseReference.child(userUid).get();
//            if (dataSnapshot.exists()) {
//                // 사용자 정보가 Realtime Database에 있으므로 기존 사용자로 처리
//                Log.d("test", "Firebase에 사용자 정보가 있음");
//                isNew = false; // 기존 사용자
//            } else {
//                // 사용자 정보가 Realtime Database에 없으므로 새로운 사용자로 처리
//                Log.d("test", "Firebase에 사용자 정보가 없음");
//            }
//        }
//
//        return isNew;
//    }



}
