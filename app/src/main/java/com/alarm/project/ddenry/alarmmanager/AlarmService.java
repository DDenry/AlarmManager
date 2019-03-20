package com.alarm.project.ddenry.alarmmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import static android.app.PendingIntent.getActivity;

public class AlarmService extends Service {

    private AlarmManager alarmManager;

    private int hour;
    private int minute;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("Service", "OnCreate!");

        super.onCreate();

        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon)).setContentTitle("").setContentText("Service is running in backend!").setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(Config.FOREGROUND_SERVICE_CODE, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Service", "OnStartCommand!");

        //
        Intent _intent = new Intent(AlarmService.this, AlarmReceiver.class);

        _intent.putExtra("APP_NAME", intent.getStringExtra("APP_NAME"));

        _intent.putExtra("APP_PACKAGE", intent.getStringExtra("APP_PACKAGE"));

        Log.i("Service", "APP_NAME is " + intent.getStringExtra("APP_NAME"));
        Log.i("Service", "APP_PACKAGE is " + intent.getStringExtra("APP_PACKAGE"));

        _intent.setAction(Config.ALARM_ACTION_SIGNAL);

        Calendar instance = Calendar.getInstance();
        //
        instance.setTimeInMillis(System.currentTimeMillis());
        //
        instance.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        instance.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("HOUR", instance.get(Calendar.HOUR_OF_DAY)));


        instance.set(Calendar.MINUTE, intent.getIntExtra("MINUTE", instance.get(Calendar.MINUTE)));

        //ATTENTION:设置SECOND后Calendar重置为1981
        //instance.set(Calendar.SECOND, intent.getIntExtra("SECONDS", new Random(36).nextInt()));

        if (intent.getStringExtra("PROCESS") != null)
            if (intent.getStringExtra("PROCESS").equals("Receiver")) {
                instance.add(Calendar.DAY_OF_MONTH, 1);
            }

        //
        while (instance.getTimeInMillis() < System.currentTimeMillis()) {
            instance.add(Calendar.DAY_OF_MONTH, 1);
        }

        //重复天数
        if (instance.get(Calendar.DAY_OF_WEEK) == 1 || instance.get(Calendar.DAY_OF_WEEK) == 7)
            instance.set(Calendar.DAY_OF_WEEK, 2);

        _intent.putExtra("HOUR", instance.get(Calendar.HOUR_OF_DAY));

        _intent.putExtra("MINUTE", instance.get(Calendar.MINUTE));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i("AlarmTime", (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DAY_OF_MONTH) + " " + instance.get(Calendar.HOUR_OF_DAY) + ":" + instance.get(Calendar.MINUTE) + ":" + instance.get(Calendar.SECOND));

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 定时任务
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(), pendingIntent);

//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Toast.makeText(this, R.string.tip_keep_service, Toast.LENGTH_LONG).show();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("Service", "OnDestroy!");

        stopForeground(true);

        //取消Alarm
        Intent _intent = new Intent(AlarmService.this, AlarmReceiver.class);

        _intent.setAction(Config.ALARM_ACTION_SIGNAL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, R.string.no_service_running, Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}
