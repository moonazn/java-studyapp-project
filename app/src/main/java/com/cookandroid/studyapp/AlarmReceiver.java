package com.cookandroid.studyapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람 소리 재생
        playAlarmSound(context, intent);

        // 알림 다이얼로그 표시
        showOverlayOrAlertDialog(context, intent.getStringExtra("MISSION"));
    }

    private void playAlarmSound(Context context, Intent intent) {
        stopAlarmSound();
        Uri soundUri = intent.getParcelableExtra("android.media.extra.NOTIFICATION");
        if (soundUri != null) {
            // MediaPlayer가 null이거나 재생 중이 아닌 경우에만 초기화
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(context, soundUri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("MediaPlayer", "Error preparing MediaPlayer: " + e.getMessage());
                }
            } else {
                // 이미 재생 중인 MediaPlayer를 중지하고 해제
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(context, soundUri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("MediaPlayer", "Error preparing MediaPlayer: " + e.getMessage());
                }
            }
        }
    }

    static void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                // MediaPlayer를 정지하고 해제
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showOverlayOrAlertDialog(Context context, String mission) {
        // 팝업을 띄우기 위한 AlertDialog.Builder 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("알람이 울렸습니다!");
        builder.setMessage("선택된 미션: " + mission);

        // 미션 수행하기 버튼 추가
        builder.setPositiveButton("미션 수행하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 미션에 따라 액티비티 이동
                performMission(context, mission);

                dialog.dismiss();
            }
        });

        // AlertDialog 생성
        AlertDialog alertDialog = builder.create();

        // Android 8.0 이상에서는 권한이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !Settings.canDrawOverlays(context)) {
            // 권한이 없으면 앱 설정 화면을 여는 인텐트 생성
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // FLAG_ACTIVITY_NEW_TASK 플래그 추가
            context.startActivity(intent);
        } else {
            // FLAG_ACTIVITY_NEW_TASK 플래그 추가
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }

            // 잠금 화면 위에 표시되도록 설정
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | Intent.FLAG_ACTIVITY_NEW_TASK);

            // AlertDialog 표시
            alertDialog.show();
        }
    }

    private void performMission(Context context, String mission) {
        // 미션에 따라 액티비티 이동
        Intent missionIntent = null;
        switch (mission) {
            case "타일 맞추기":
                missionIntent = new Intent(context, TileActivity.class);
                break;
            case "숫자 계산":
                missionIntent = new Intent(context, NumCalcActivity.class);
                break;
            case "명언 쓰기":
                missionIntent = new Intent(context, WiseSayingActivity.class);
                break;
            default:
                // 기본적으로 어떤 미션도 해당하지 않을 때의 동작 설정
                break;
        }

        if (missionIntent != null) {
            missionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(missionIntent);
        }
    }
    public static void cancelAlarm(Context context, int alarmId, boolean calledFromActivity) {
        // 액티비티에서 호출된 경우, 알람 리시버와 관련된 작업을 수행하지 않습니다.
        if (calledFromActivity) {
            return;
        }

        // 메서드의 나머지 부분은 변경되지 않습니다.
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    alarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    alarmIntent,
                    0
            );
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        stopAlarmSound();
    }

}