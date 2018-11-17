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
    private TextView textView_time;
    private TextView textView_info;

    private Boolean serviceStarted;
    private Handler handler;

    private ListView listView_app;
    private ListAdapter listAdapter;

    private List<ResolveInfo> resolveInfos;

    private String appPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        InitComponents();

        serviceStarted = isServiceRunning(MainActivity.this, getPackageName() + ".AlarmService");

        Log.i("Process", "Alarm service's state is " + serviceStarted);

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

        //TODO:异步获取第三方应用信息
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
        button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new OnClickListener());

        button_stop = findViewById(R.id.button_stop);
        button_stop.setOnClickListener(new OnClickListener());

        textView_time = findViewById(R.id.textView_time);

        textView_info = findViewById(R.id.textView_info);

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
        PackageManager packageManager = getPackageManager();
        //匹配程序的入口
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //查询
        resolveInfos = packageManager.queryIntentActivities(intent, 0);
        //
        listAdapter = new ListAdapter(MainActivity.this, resolveInfos);

        listView_app.setAdapter(listAdapter);
        //
        listView_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

                    button_start.setText("Service is running ……");
                    button_start.setEnabled(false);

                    service.putExtra("APP_PACKAGE", appPackageName);

                    //
                    service.putExtra("HOUR", 10);
                    service.putExtra("MINUTE", new Random().nextInt(5) + 20);

                    //TODO:开启服务
                    startService(service);
                    break;
                case R.id.button_stop:
                    button_stop.setEnabled(false);

                    button_start.setText("Start Service");
                    button_start.setEnabled(true);

                    //TODO:关闭服务
                    stopService(service);
                    break;
            }

        }
    }
}

