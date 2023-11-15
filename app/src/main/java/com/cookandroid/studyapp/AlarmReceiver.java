package com.cookandroid.studyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람이 울릴 때 실행할 동작을 정의
        // 예를 들어, 알림을 표시하거나 원하는 동작을 수행

        // NotificationHelper 클래스를 사용하여 알림을 표시
        // NotificationHelper notificationHelper = new NotificationHelper(context);
        // notificationHelper.createNotification("알람", "알람이 울렸습니다.");

        Uri soundUri = intent.getParcelableExtra("android.media.extra.NOTIFICATION");
        if (soundUri != null) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(context, soundUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(context, "알람이 울렸습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}