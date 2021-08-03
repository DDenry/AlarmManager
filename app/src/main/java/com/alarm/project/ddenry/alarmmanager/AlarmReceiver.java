package com.alarm.project.ddenry.alarmmanager;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import static android.content.Context.KEYGUARD_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Config.ALARM_ACTION_SIGNAL)) {

            Log.i("Receiver", "Have received broadcast!");

            //获取wifi管理服务
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                //获取wifi开关状态
                int status = wifiManager.getWifiState();

                if (status == WifiManager.WIFI_STATE_DISABLED) {
                    wifiManager.setWifiEnabled(true);
                }
            }

            //判断当前设备情景模式
            int ringerMode = ((AudioManager) Objects.requireNonNull(context.getSystemService(Context.AUDIO_SERVICE))).getRingerMode();

            Log.i("RingerMode", "Current device's ringerMode is " + ringerMode);

            switch (ringerMode) {
                //0
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                //1
                case AudioManager.RINGER_MODE_VIBRATE:
                    break;
                //2
                case AudioManager.RINGER_MODE_NORMAL:
                    break;
            }

            //唤醒CPU
            PowerManager powerManager = (PowerManager) context.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);

            PowerManager.WakeLock wakeLock = null;

            if (powerManager != null) {

                //唤醒屏幕
                wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "AlarmManager:WakeLock");
                wakeLock.acquire(3 * 60 * 1000L);

                wakeLock.setReferenceCounted(false);
            }

            KeyguardManager keyguardManager = (KeyguardManager) context
                    .getSystemService(KEYGUARD_SERVICE);

            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");

            //解锁
            keyguardLock.disableKeyguard();

            Toast.makeText(context, "Try 2 open app " + intent.getStringExtra("APP_NAME"), Toast.LENGTH_LONG).show();

            //根据包名打开指定应用
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(intent.getStringExtra("APP_PACKAGE")));

            if (wakeLock != null) {
                wakeLock.release();
            }

            //
            Intent _intent = new Intent(context, AlarmService.class);
            _intent.putExtra("PROCESS", "Receiver");
            _intent.putExtra("APP_NAME", intent.getStringExtra("APP_NAME"));
            _intent.putExtra("APP_PACKAGE", intent.getStringExtra("APP_PACKAGE"));
            _intent.putExtra("HOUR", intent.getIntExtra("HOUR", 0));
            _intent.putExtra("MINUTE", intent.getIntExtra("MINUTE", 0));

            context.startService(_intent);

            //锁屏
            keyguardLock.reenableKeyguard();
        }
    }
}
