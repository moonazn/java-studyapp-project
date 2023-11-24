package com.cookandroid.studyapp;


import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Uri soundUri;
    private int alarmHour;
    private int alarmMinute;

    public AlarmService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 알람이 울릴 때 실행되는 코드
        soundUri = intent.getParcelableExtra("android.media.extra.NOTIFICATION");
        alarmHour = intent.getIntExtra("ALARM_HOUR", 0);
        alarmMinute = intent.getIntExtra("ALARM_MINUTE", 0);

        Log.d("AlarmService", "Alarm time: " + alarmHour + ":" + alarmMinute);

        // 현재 시간을 확인
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        Log.d("AlarmService", "Current time: " + currentHour + ":" + currentMinute);

        if ((currentHour <= alarmHour && currentMinute < alarmMinute)) {
            // 알람 시간과 현재 시간이 일치하면 소리를 울림
            if (currentHour == alarmHour && currentMinute == alarmMinute) {
                Log.d("AlarmService", "Playing alarm sound");
                playAlarmSound();
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playAlarmSound() {
        if (soundUri != null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, soundUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.prepare();
                mediaPlayer.start();


            } catch (IOException e) {
                Log.e("AlarmService", "Error playing alarm sound: " + e.getMessage());
            }
        }
    }
}