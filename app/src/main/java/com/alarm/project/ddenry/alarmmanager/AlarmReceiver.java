package com.alarm.project.ddenry.alarmmanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Config.ALARM_ACTION_SIGNAL)) {

            Log.i("Receiver", "Have received broadcast!");

            Toast.makeText(context, "Try 2 open app " + intent.getStringExtra("APP_NAME"), Toast.LENGTH_LONG).show();

            //根据包名打开指定应用
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(intent.getStringExtra("APP_PACKAGE")));

            //
            Intent _intent = new Intent(context, AlarmService.class);
            _intent.putExtra("PROCESS", "Receiver");
            _intent.putExtra("APP_NAME", intent.getStringExtra("APP_NAME"));
            _intent.putExtra("APP_PACKAGE", intent.getStringExtra("APP_PACKAGE"));
            _intent.putExtra("HOUR", intent.getIntExtra("HOUR", 0));
            _intent.putExtra("MINUTE", intent.getIntExtra("MINUTE", 0));

            context.startService(_intent);
        }
    }
}
