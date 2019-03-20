package com.alarm.project.ddenry.alarmmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private Button button_start;
    private Button button_stop;
    private TextView textView_title;
    private TextView textView_time;
    private TextView textView_info;
    private TimePicker timePicker;

    private int hour;
    private int _minute;

    private Handler handler;

    private ListView listView_app;

    private PackageManager packageManager;

    private List<ResolveInfo> resolveInfos;

    private String appName;
    private String appPackageName;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("packageName", appPackageName);
        outState.putInt("hour", hour);
        outState.putInt("minute", _minute);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        InitComponents();

        boolean serviceStarted = isServiceRunning(MainActivity.this, getPackageName() + ".AlarmService");

        Log.i("Process", "Alarm service's state is " + serviceStarted);

        if (serviceStarted) {

            if (savedInstanceState != null) {

                String packageName = savedInstanceState.getString("packageName");

                appPackageName = packageName;

                Log.i("Process", "Current service is running of " + packageName);
                Log.i("Process", "Scheduled time is " + savedInstanceState.getInt("hour") + ":" + savedInstanceState.getInt("minute"));

                //
                timePicker.setHour(savedInstanceState.getInt("hour"));
                timePicker.setMinute(savedInstanceState.getInt("minute"));
                timePicker.setEnabled(false);

                textView_info.setText(packageName);

                button_stop.setEnabled(true);

                button_start.setText(R.string.service_running);
                button_start.setEnabled(false);

                listView_app.setVisibility(View.GONE);

                textView_title.setText(getResources().getString(R.string.service_running));
            }
        }

        //
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    //Show real time
                    case 0:
                        textView_time.setText(new Date().toString());
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        //异步获取第三方应用信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                DigThirdAppInfos();
            }
        }).start();

        //
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);
    }

    protected void InitComponents() {

        textView_title = findViewById(R.id.textView_title);

        button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new OnClickListener());

        button_stop = findViewById(R.id.button_stop);
        button_stop.setOnClickListener(new OnClickListener());

        textView_time = findViewById(R.id.textView_time);

        textView_info = findViewById(R.id.textView_info);

        timePicker = findViewById(R.id.timePicker);
        //
        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour = hourOfDay;
                _minute = minute;
            }
        });

        listView_app = findViewById(R.id.listView_app);
    }

    public boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    protected void DigThirdAppInfos() {
        packageManager = getPackageManager();
        //匹配程序的入口
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //查询
        resolveInfos = packageManager.queryIntentActivities(intent, 0);
        //
        ListAdapter listAdapter = new ListAdapter(MainActivity.this, resolveInfos);

        listView_app.setAdapter(listAdapter);
        //
        listView_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                position -= Config.EXTRA_ITEM_COUNT;

                appName = resolveInfos.get(position).activityInfo.loadLabel(packageManager).toString();

                textView_info.setText(appPackageName = resolveInfos.get(position).activityInfo.packageName);

                button_start.setEnabled(!appPackageName.equals(""));
            }
        });
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent service = new Intent(MainActivity.this, AlarmService.class);
            switch (v.getId()) {
                case R.id.button_start:

                    button_stop.setEnabled(true);

                    button_start.setText(R.string.service_running);
                    button_start.setEnabled(false);

                    timePicker.setEnabled(false);

                    service.putExtra("APP_NAME", appName);
                    service.putExtra("APP_PACKAGE", appPackageName);

                    //
                    service.putExtra("HOUR", hour);
                    service.putExtra("MINUTE", _minute);

                    listView_app.setVisibility(View.GONE);

                    textView_title.setText(getResources().getString(R.string.service_running));

                    //开启服务
                    startService(service);

                    break;
                case R.id.button_stop:
                    button_stop.setEnabled(false);

                    button_start.setText(R.string.start_service);
                    button_start.setEnabled(true);

                    //关闭服务
                    stopService(service);

                    listView_app.setVisibility(View.VISIBLE);

                    textView_title.setText(getResources().getString(R.string.tip_title));

                    timePicker.setEnabled(true);

                    break;
            }

        }
    }
}

