package com.cookandroid.studyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ResetStopwatch extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람이 울릴 때마다 스톱워치 초기화
        SharedPreferences sharedPreferences = context.getSharedPreferences("StopwatchPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stopwatchTime", 0);
        editor.apply();
    }
}