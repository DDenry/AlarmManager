package com.alarm.project.ddenry.alarmmanager;

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

            //TODO:根据包名打开指定应用
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(intent.getStringExtra("APP_PACKAGE")));
        }
    }
}
