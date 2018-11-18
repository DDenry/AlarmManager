package com.alarm.project.ddenry.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class AlarmService extends Service {

    private AlarmManager alarmManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("Service", "OnCreate!");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Service", "OnStartCommand!");

        Intent _intent = new Intent(AlarmService.this, AlarmReceiver.class);

        _intent.putExtra("APP_NAME", intent.getStringExtra("APP_NAME"));

        _intent.putExtra("APP_PACKAGE", intent.getStringExtra("APP_PACKAGE"));

        Log.i("Service", "APP_NAME is " + intent.getStringExtra("APP_NAME"));
        Log.i("Service", "APP_PACKAGE is " + intent.getStringExtra("APP_PACKAGE"));

        _intent.setAction(Config.ALARM_ACTION_SIGNAL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar instance = Calendar.getInstance();
        //
        instance.setTimeInMillis(System.currentTimeMillis());
        //
        instance.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        instance.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("HOUR", instance.get(Calendar.HOUR_OF_DAY)));

        instance.set(Calendar.MINUTE, intent.getIntExtra("MINUTE", instance.get(Calendar.MINUTE)));

        //ATTENTION:设置SECOND后Calendar重置为1981
        //instance.set(Calendar.SECOND, intent.getIntExtra("SECONDS", new Random(36).nextInt()));

        //
        if (instance.getTimeInMillis() < System.currentTimeMillis()) {
            instance.add(Calendar.DAY_OF_MONTH, 1);
        }

        //重复天数
        if (instance.get(Calendar.DAY_OF_WEEK) == 1 || instance.get(Calendar.DAY_OF_WEEK) == 7)
            instance.set(Calendar.DAY_OF_WEEK, 2);

        Log.i("AlarmTime", (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DAY_OF_MONTH) + " " + instance.get(Calendar.HOUR_OF_DAY) + ":" + instance.get(Calendar.MINUTE) + ":" + instance.get(Calendar.SECOND));

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Toast.makeText(this, "Please keep the service running for good work", Toast.LENGTH_LONG).show();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("Service", "OnDestroy!");
        //TODO:取消Alarm
        Intent _intent = new Intent(AlarmService.this, AlarmReceiver.class);

        _intent.setAction(Config.ALARM_ACTION_SIGNAL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "There's no service running~", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}
