package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.AppLockActivity.selectedUnlockApps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class AppLockINGActivity extends AppCompatActivity {
    private TextView remainingTimeTextView;
    private ListView availableAppsListView;
    private Button stopLock;
    private TextView back;

    private long remainingTimeMillis; // 남은 시간(밀리초)을 저장하는 변수

    private Handler handler;
    private Runnable updateRemainingTimeRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock_ingactivity);

        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        availableAppsListView = findViewById(R.id.availableAppsListView);
        stopLock = findViewById(R.id.stopLockButton);
        back = findViewById(R.id.backButton);

        handler = new Handler();

        // remainingTimeMillis을 초기화하고 실제로는 여기서 남은 시간을 계산할 로직을 추가하세요.
        // 예를 들어, unlockHour와 unlockMinute을 기반으로 남은 시간을 계산할 수 있습니다.
        remainingTimeMillis = calculateRemainingTimeMillis();

        // Runnable을 이용하여 남은 시간을 주기적으로 업데이트합니다.
        updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                remainingTimeMillis -= 1000; // 1초(1000밀리초)를 뺍니다.

                // remainingTimeMillis을 HH:mm:ss 형식으로 변환하고 TextView에 설정
                String remainingTimeText = formatRemainingTime(remainingTimeMillis);
                remainingTimeTextView.setText("앱 잠금 종료까지: " + remainingTimeText);

                // 남은 시간이 0보다 크면 다시 Runnable을 실행하여 1초마다 업데이트
                if (remainingTimeMillis > 0) {
                    handler.postDelayed(this, 1000); // 1초(1000밀리초)마다 업데이트
                }
            }
        };

        // Runnable을 시작하여 남은 시간을 업데이트
        handler.post(updateRemainingTimeRunnable);


        // Create a list of available apps (not locked)
        List<AppInfo> availableAppsList = createAvailableAppsList();

        // Set up the available apps list using an adapter
        AppInfoAdapter availableAppsAdapter = new AppInfoAdapter(this, R.layout.app_list_item, availableAppsList);
        availableAppsListView.setAdapter(availableAppsAdapter);

        stopLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent serviceIntent = new Intent(AppLockINGActivity.this, TimerService.class);
                serviceIntent.putExtra("stopTimer", true);
                startService(serviceIntent);

                Intent intent = new Intent(AppLockINGActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppLockINGActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 중지될 때 Runnable을 중지
        handler.removeCallbacks(updateRemainingTimeRunnable);
    }

    // Calculate the remaining time in milliseconds (implement your own logic)
    // 남은 시간을 밀리초로 계산 (예시로 30분으로 설정)
    private long calculateRemainingTimeMillis() {
        return 30 * 60 * 1000; // 30분을 밀리초로 변환
    }

    // 밀리초를 HH:mm 형식으로 변환
    private String formatRemainingTime(long remainingTimeMillis) {

        long minutes = (remainingTimeMillis / (1000 * 60)) % 60;
        long hours = (remainingTimeMillis / (1000 * 60 * 60));

        // HH:mm:ss 형식으로 포맷
        return String.format("%02d시간 %02d분", hours, minutes);
    }

    // Create a list of available apps (not locked)
    private List<AppInfo> createAvailableAppsList() {

        // These are the apps that are not locked and can be used
        List<AppInfo> availableAppsList = new ArrayList<>();

        for (AppInfo selectedUnlockApp : selectedUnlockApps) {
            if (selectedUnlockApp != null) {
                availableAppsList.add(selectedUnlockApp);
            }
        }

        return availableAppsList;
    }

}
