package com.cookandroid.studyapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppLockActivity extends AppCompatActivity {
    private RecyclerView appListRecyclerView;
    private AppRecyclerViewAdapter appListAdapter;
    private List<AppInfo> appList = new ArrayList<>();

    private RecyclerView lockAppListRecyclerView;
    private AppRecyclerViewAdapter lockAppListAdapter;
    private List<AppInfo> lockAppList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        appListRecyclerView = findViewById(R.id.appList);
        appListAdapter = new AppRecyclerViewAdapter(appList);
        appListRecyclerView.setAdapter(appListAdapter);
        appListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        lockAppListRecyclerView = findViewById(R.id.unlockAppList);
        lockAppListAdapter = new AppRecyclerViewAdapter(lockAppList);
        lockAppListRecyclerView.setAdapter(lockAppListAdapter);
        lockAppListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 앱 목록 데이터 초기화
        loadInstalledApps();

        // "앱 잠금 시작" 버튼 클릭 시, lockAppList에 있는 앱만 잠그도록 로직 추가
        Button lockButton = findViewById(R.id.lockButton);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (AppInfo appInfo : lockAppList) {
                    if (appInfo.isSelected()) {
                        // 앱을 잠금 처리하는 로직 추가
                    }
                }
            }
        });
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            String appName = resolveInfo.loadLabel(pm).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            Drawable appIcon = resolveInfo.loadIcon(pm);

            AppInfo appInfo = new AppInfo(packageName, appName, appIcon);
            appList.add(appInfo);
        }

        appListAdapter.notifyDataSetChanged(); // 데이터가 업데이트되었음을 어댑터에 알림
    }
}
