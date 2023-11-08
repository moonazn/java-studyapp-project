package com.cookandroid.studyapp;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TimerService extends Service {
    private Handler handler;
    private Runnable unlockAppRunnable;
    private static boolean isTimerRunning = false;

    private int unlockHour = 0; // 사용자가 설정한 시간 (기본값 0)
    private int unlockMinute = 0; // 사용자가 설정한 분 (기본값 0)

    private List<AppInfo> lockedApps; // 잠금/해제 앱 목록

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

        unlockAppRunnable = new Runnable() {
            @Override
            public void run() {
                // 현재 시간을 가져옴
                long currentTime = System.currentTimeMillis();
                long unlockTime = getUnlockTime(); // 사용자가 설정한 시간으로 변환

                if (currentTime >= unlockTime) {
                    // 앱 잠금 해제 로직
                    isTimerRunning = false;
                    unlockApp();
                    stopSelf(); // 서비스 종료
                }

                // 타이머를 주기적으로 실행
                handler.postDelayed(this, 60000); // 1분마다 실행 (60,000 밀리초)
            }
        };
        isTimerRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getBooleanExtra("stopTimer", false)) {
            // 타이머 중지와 앱 잠금 해제 요청 처리
            stopTimerAndUnlockApps();
            stopSelf(); // 서비스 종료
        } else {
            // 사용자가 설정한 시간을 가져옴
            unlockHour = intent.getIntExtra("unlockHour", 0);
            unlockMinute = intent.getIntExtra("unlockMinute", 0);

            // 잠금/해제 앱 목록을 가져옴
            lockedApps = intent.getParcelableArrayListExtra("lockedApps");

            // 타이머 시작
            handler.post(unlockAppRunnable);
            isTimerRunning = true;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 타이머 중지
        isTimerRunning = false;
        handler.removeCallbacks(unlockAppRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long getUnlockTime() {
        // 사용자가 설정한 시간을 밀리초로 변환
        long currentTimeMillis = System.currentTimeMillis();
        long unlockTimeMillis = currentTimeMillis;

        unlockTimeMillis = unlockTimeMillis / 60000 * 60000; // 초와 밀리초 제거
        unlockTimeMillis += (unlockHour * 60 + unlockMinute) * 60000; // 사용자가 설정한 시간 추가

        return unlockTimeMillis;
    }

    private void stopTimerAndUnlockApps() {
        // 타이머 중지
        isTimerRunning = false;
        handler.removeCallbacks(unlockAppRunnable);

        // 모든 앱을 잠금 해제
        unlockApp();
    }

    public static boolean isTimerRunning() {
        return isTimerRunning;
    }

    private void unlockApp() {
        // 앱 잠금 해제 로직을 구현

        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(TimerService.this, MyDeviceAdminReceiver.class);

        for (AppInfo appInfo : lockedApps) {
            // 앱 잠금 해제 - 특정 앱을 잠금 해제
            mDevicePolicyManager.setApplicationHidden(adminComponent, appInfo.getPackageName(), false);
            Log.d("applock", appInfo.getName() + " - 숨김 x");
        }

        // 앱 잠금이 해제되면 사용자에게 알림을 표시
        String message = "앱 잠금 종료";
        Toast.makeText(TimerService.this, message, Toast.LENGTH_SHORT).show();

    }


}
