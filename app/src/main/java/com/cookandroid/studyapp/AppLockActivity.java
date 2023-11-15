package com.cookandroid.studyapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppLockActivity extends AppCompatActivity {

    private ListView appList;
    private ListView unlockAppList;
    private List<AppInfo> appInfoList;
    private List<AppInfo> unlockAppInfoList;
    private AppInfoAdapter appInfoAdapter;
    private AppInfoAdapter unlockAppInfoAdapter;

    static List<AppInfo> selectedUnlockApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        appList = findViewById(R.id.appList);
        unlockAppList = findViewById(R.id.unlockAppList);

        appInfoList = new ArrayList<>();
        unlockAppInfoList = new ArrayList<>();

        appInfoAdapter = new AppInfoAdapter(this, R.layout.app_list_item, appInfoList);
        unlockAppInfoAdapter = new AppInfoAdapter(this, R.layout.app_list_item, unlockAppInfoList);

        appList.setAdapter(appInfoAdapter);
        unlockAppList.setAdapter(unlockAppInfoAdapter);

        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);

        appInfoList.clear(); // 기존 앱 목록을 초기화

        for (ResolveInfo resolveInfo : resolveInfoList) {
            String appName = resolveInfo.loadLabel(pm).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            Drawable appIcon = resolveInfo.loadIcon(pm);

            // 이전에 저장한 토글 상태를 가져와서 설정
            boolean isSelected = false; // 기본값은 비선택 상태로 설정
            for (AppInfo appInfo : appInfoList) {
                if (appInfo.getPackageName().equals(packageName)) {
                    isSelected = appInfo.isSelected();
                    break;
                }
            }

            AppInfo appInfo = new AppInfo(packageName, appName, appIcon, R.drawable.lock);
            appInfo.setSelected(isSelected);
            appInfoList.add(appInfo);
        }

        // StudyApp, Phone, Camera 앱을 unlockAppInfoList에 추가
        addDefaultUnlockApps("com.cookandroid.studyapp", unlockAppInfoList);
        addDefaultUnlockApps("com.android.dialer", unlockAppInfoList);
        addDefaultUnlockApps("com.android.camera2", unlockAppInfoList);

        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo selectedApp = appInfoAdapter.getItem(position);

                if (selectedApp != null) {
                    appInfoList.remove(selectedApp);
                    Log.d("cameraPN", selectedApp.getPackageName());
                    selectedApp.setAppSelectImage(R.drawable.unlock);
                    unlockAppInfoList.add(selectedApp);
                    appInfoAdapter.notifyDataSetChanged();
                    unlockAppInfoAdapter.notifyDataSetChanged();
                }
            }
        });

        unlockAppList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo selectedApp = unlockAppInfoAdapter.getItem(position);

                if (selectedApp != null) {
                    unlockAppInfoList.remove(selectedApp);
                    selectedApp.setAppSelectImage(R.drawable.lock);
                    appInfoList.add(selectedApp);
                    appInfoAdapter.notifyDataSetChanged();
                    unlockAppInfoAdapter.notifyDataSetChanged();
                }
            }
        });

        Button lockButton = findViewById(R.id.lockButton);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "unlockAppList"에 표시된 앱들을 가져옴
                selectedUnlockApps = new ArrayList<>(unlockAppInfoList);

                // "unlockAppList"에 표시된 앱들을 제외한 모든 앱들을 가져옴
                List<AppInfo> lockedApps = new ArrayList<>(appInfoList);
                lockedApps.removeAll(selectedUnlockApps);

                // "timePicker"에서 선택한 시간을 가져오는 코드
                TimePicker timePicker = findViewById(R.id.timePicker);
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                // 현재 시간을 가져오는 코드
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // 선택한 시간과 현재 시간을 비교
                if (hour > currentHour || (hour == currentHour && minute > currentMinute)) {
                    // 선택한 시간이 현재 시간 이후인 경우
                    // 잠금 상태로 변경
                    for (AppInfo appInfo : appInfoList) {
                        appInfo.setLocked(true);
                    }

                    Intent serviceIntent = new Intent(AppLockActivity.this, TimerService.class);
                    ArrayList<AppInfo> appInfoArrayList = new ArrayList<>(appInfoList);
                    serviceIntent.putParcelableArrayListExtra("lockedApps", appInfoArrayList); // ArrayList를 전달
                    serviceIntent.putExtra("unlockHour", hour);
                    serviceIntent.putExtra("unlockMinute", minute);
                    startService(serviceIntent);

                } else {
                    // 선택한 시간이 현재 시간 이전인 경우
                    // 잠금 해제 상태로 변경
                    for (AppInfo appInfo : appInfoList) {
                        appInfo.setLocked(false);
                    }
                }

                // "appInfoAdapter"를 업데이트하여 변경된 잠금 상태를 반영
                appInfoAdapter.notifyDataSetChanged();

                // 여기에서는 AppInfo 클래스에 setLocked(boolean locked) 메서드를 추가하여 사용합니다.
                for (AppInfo appInfo : lockedApps) {
                    appInfo.setLocked(true);
                    Log.d("setLocked", "true");
                }

                // "appInfoAdapter" 및 "unlockAppInfoAdapter"를 업데이트
                appInfoAdapter.notifyDataSetChanged();
                unlockAppInfoAdapter.notifyDataSetChanged();

                DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName adminComponent = new ComponentName(AppLockActivity.this, MyDeviceAdminReceiver.class);

                // 선택된 앱들을 잠금 설정하고, 선택되지 않은 앱들을 잠금 해제하도록 수정
                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.isLocked()) {
                        // 앱을 잠금 처리

                        // 앱 잠금 - 패키지 이름을 사용하여 특정 앱을 잠금
                        mDevicePolicyManager.setApplicationHidden(adminComponent, appInfo.getPackageName(), true);

                        Intent intent = getPackageManager().getLaunchIntentForPackage(appInfo.getPackageName());
                        if (intent != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(AppLockActivity.this, "앱 실행 불가", Toast.LENGTH_SHORT).show();
                        }

                        Log.d("applock", appInfo.getName() + " - 잠금 완료");
                    } else {
                        // 앱을 잠금 해제 처리
                        // 앱 잠금 해제 - 특정 앱을 잠금 해제
                        mDevicePolicyManager.setApplicationHidden(adminComponent, appInfo.getPackageName(), false);
                        Log.d("applock", appInfo.getName() + " - 잠금 해제");
                    }
                }

                Intent intent = new Intent(AppLockActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void addDefaultUnlockApps(String packageName, List<AppInfo> unlockApps) {
        List<AppInfo> appsToRemove = new ArrayList<>();

        for (AppInfo appInfo : appInfoList) {
            if (appInfo.getPackageName().equals(packageName)) {
                appInfo.setAppSelectImage(R.drawable.unlock);
                unlockApps.add(appInfo);
                appsToRemove.add(appInfo);
            }
        }

        // appInfoList에서 appsToRemove에 있는 앱을 삭제
        appInfoList.removeAll(appsToRemove);
    }
}
