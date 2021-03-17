package com.alarm.project.ddenry.alarmmanager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;

public class AlarmService extends Service {

    private AlarmManager alarmManager;
    private ActivityManager activityManager;
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("Service", "OnCreate!");

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        super.onCreate();

        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this.getApplicationContext(), String.valueOf(Config.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL));

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(String.valueOf(Config.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL), Config.FOREGROUND_SERVICE_CHANNEL_NAME, importance);
            channel.setDescription(Config.FOREGROUND_SERVICE_CHANNEL_NAME);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else builder = new NotificationCompat.Builder(this.getApplicationContext());

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);

        builder.setContentTitle("Be silent ...")
                .setContentText("It's coming~")
                .setLargeIcon(icon)
                .setSmallIcon(R.mipmap.icon)
                .setPriority(PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setTicker("Ticker")
                .setAutoCancel(false);

        startForeground(new Random().nextInt(20), builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Service", "OnStartCommand!");

        //
        Intent _intent = new Intent(AlarmService.this, AlarmReceiver.class);

        _intent.putExtra("APP_NAME", intent.getStringExtra("APP_NAME"));

        final String appPackageName = intent.getStringExtra("APP_PACKAGE");

        _intent.putExtra("APP_PACKAGE", appPackageName);

        Log.i("Service", "APP_NAME is " + intent.getStringExtra("APP_NAME"));
        Log.i("Service", "APP_PACKAGE is " + appPackageName);

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
                //
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //回到主界面
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);

                            Log.i("Intent", "ACTION_MAIN");
                        } catch (ActivityNotFoundException e) {
                            Log.e("Exception", e.getMessage());
                        }

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //停止后台进程
                        activityManager.killBackgroundProcesses(appPackageName);

                        Log.i("Service", "Has stopped app " + appPackageName);

                        //重启自身应用
                        Log.i("Restart", getPackageName());
                        startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
                    }
                }, 30 * 1000);
            }

        //
        while (instance.getTimeInMillis() < System.currentTimeMillis()) {
            instance.add(Calendar.DAY_OF_MONTH, 1);
        }

        //工作日重复，周六日不重复
        while (instance.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || instance.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            instance.add(Calendar.DAY_OF_MONTH, 1);

        _intent.putExtra("HOUR", instance.get(Calendar.HOUR_OF_DAY));

        _intent.putExtra("MINUTE", instance.get(Calendar.MINUTE));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String alarmTime = (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DAY_OF_MONTH) + " " + instance.get(Calendar.HOUR_OF_DAY) + ":" + instance.get(Calendar.MINUTE) + ":" + instance.get(Calendar.SECOND);

        Log.i("AlarmTime", alarmTime);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 定时任务
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(), pendingIntent);

//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

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

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

//        Toast.makeText(this, R.string.no_service_running, Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}
