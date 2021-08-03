package com.alarm.project.ddenry.alarmmanager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private Button button_start;

    private Button button_stop;

    private ToggleButton toggleButton;

    private TextView textView_title;

    private TextView textView_info;

    private static int hour;

    private static int minute;

    private StaticHandler handler;

    private ListView listView_app;

    private PackageManager packageManager;

    private List<ResolveInfo> resolveInfo;

    private String appName;

    private String appPackageName;

    private LinearLayout linearLayout;

    //目标Service，全局唯一
    private Intent service;

    private ListenServer listenServer;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("packageName", appPackageName);
        outState.putInt("hour", hour);
        outState.putInt("minute", minute);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //
        packageManager = getPackageManager();

        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.KILL_BACKGROUND_PROCESSES) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "No permission");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.KILL_BACKGROUND_PROCESSES}, 100);
            }
        }

        //初始化控件
        initComponents();

        boolean serviceStarted = isServiceRunning(MainActivity.this, getPackageName() + ".AlarmService");

        Log.i("Process", "Alarm service's state is " + serviceStarted);

        if (serviceStarted) {

            if (savedInstanceState != null) {

                String packageName = savedInstanceState.getString("packageName");

                appPackageName = packageName;

                Log.i("Process", "Current service is running of " + packageName);
                Log.i("Process", "Scheduled time is " + savedInstanceState.getInt("hour") + ":" + savedInstanceState.getInt("minute"));

                //
                hour = savedInstanceState.getInt("hour");
                minute = savedInstanceState.getInt("minute");

                textView_info.setText(packageName);

                button_start.setText(R.string.service_running);
                button_start.setEnabled(false);

                button_stop.setEnabled(true);

                listView_app.setVisibility(View.GONE);

                textView_title.setText(getResources().getString(R.string.service_running));
            }
        }

        //静态内部类，继承Handler
        handler = new StaticHandler(this);

        //异步获取第三方应用信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                digThirdAppInfo();
            }
        }).start();

        //更新UI时间
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);

        selfCheck();
    }

    protected void selfCheck() {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            Log.i("PackageInfo", "VersionName:" + versionName);
            Log.i("PackageInfo", "VersionCode:" + versionCode);
            Log.i("AndroidSDKInfo", android.os.Build.VERSION.RELEASE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void initComponents() {

        linearLayout = findViewById(R.id.linearLayout);

        textView_title = findViewById(R.id.textView_title);

        button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new OnClickListener());

        button_stop = findViewById(R.id.button_stop);
        button_stop.setOnClickListener(new OnClickListener());

        toggleButton = findViewById(R.id.toggleButton_remote);

        textView_info = findViewById(R.id.textView_info);

        ImageView imageView_clock = findViewById(R.id.imageView_clock);

        imageView_clock.setOnClickListener(new OnClickListener());

        listView_app = findViewById(R.id.listView_app);
    }

    public boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = null;

        if (myManager != null) {
            runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                    .getRunningServices(30);
        }
        if (runningService != null) {
            for (int i = 0; i < runningService.size(); i++) {
                if (runningService.get(i).service.getClassName()
                        .equals(ServiceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void digThirdAppInfo() {

        //匹配程序的入口
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //查询
        resolveInfo = packageManager.queryIntentActivities(intent, 0);
        //
        ListAdapter listAdapter = new ListAdapter(MainActivity.this, resolveInfo);

        listView_app.setAdapter(listAdapter);
        //
        listView_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                position -= Config.EXTRA_ITEM_COUNT;

                appName = resolveInfo.get(position).activityInfo.loadLabel(packageManager).toString();

                textView_info.setText(appPackageName = resolveInfo.get(position).activityInfo.packageName);

                button_start.setEnabled(!appPackageName.equals(""));
            }
        });
    }

    private void showSnackBar() {

        Snackbar.make(linearLayout, getResources().getString(R.string.alarm_time) + " " + hour + ":" + minute, Snackbar.LENGTH_SHORT).setAction("DDenry~", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SnackBar", "Button clicked!");
            }
        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                Log.i("SnackBar", event + "");
            }
        }).show();
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.imageView_clock:

                    if (listView_app.getVisibility() == View.VISIBLE)
                        new TimePickerDialog(MainActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        hour = hourOfDay;
                                        MainActivity.minute = minute;
                                    }
                                }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true).show();
                    else showSnackBar();

                    break;

                case R.id.button_start:

                    setupService();

                    //
                    listenRemote(toggleButton.isChecked());

                    break;
                case R.id.button_stop:

                    shutdownService();

                    break;
            }
        }

        //开启服务
        private void setupService() {

            if (service != null) stopService(service);

            button_stop.setEnabled(true);
            button_start.setText(R.string.service_running);
            button_start.setEnabled(false);
            toggleButton.setEnabled(false);
            listView_app.setVisibility(View.GONE);
            textView_title.setText(getResources().getString(R.string.service_running));

            //实例化Service
            service = new Intent(MainActivity.this, AlarmService.class);

            //应用名称
            service.putExtra("APP_NAME", appName);
            //应用包名
            service.putExtra("APP_PACKAGE", appPackageName);
            //
            setupService(MainActivity.hour, MainActivity.minute);
        }

        private void setupService(int hour, int minute) {
            if (service == null) return;

            stopService(service);

            //几时
            service.putExtra("HOUR", hour);
            //几分
            service.putExtra("MINUTE", minute);
            //开启服务
//            startService(service);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
            //显示SnackBar
            showSnackBar();
        }

        //关闭服务
        private void shutdownService() {

            if (service != null)
                //关闭服务
                stopService(service);

            button_stop.setEnabled(false);
            button_start.setText(R.string.start_service);
            button_start.setEnabled(true);
            toggleButton.setEnabled(true);
            listView_app.setVisibility(View.VISIBLE);
            textView_title.setText(getResources().getString(R.string.tip_title));
        }

        private ListenServer asyncTaskPop() {
            return new ListenServer(new AsyncTaskDone() {
                @Override
                public void onSucceed() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //生效远程配置
                            setupService(hour, minute);
                        }
                    }, 1000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            asyncTaskPop().execute();
                        }
                    }, 30 * 60 * 1000);
                }

                @Override
                public void onFailed() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            asyncTaskPop().execute();
                        }
                    }, 60 * 1000);
                }
            });
        }

        //远程监听
        private void listenRemote(boolean inNeed) {
            if (inNeed) {
                if (listenServer != null) {
                    listenServer.cancel(true);
                    listenServer = null;
                }

                //ListenServer AsyncTask
                listenServer = asyncTaskPop();

                listenServer.execute();

            } else {
                if (listenServer == null) return;

                //强制停止
                listenServer.cancel(true);
                if (listenServer.isCancelled()) listenServer = null;
            }
        }
    }

    static class StaticHandler extends Handler {

        WeakReference<Activity> weakReference;

        StaticHandler(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = weakReference.get();

            if (activity == null) return;

            //Show real time
            if (msg.what == 0) {
                ((TextView) activity.findViewById(R.id.textView_time)).setText(new Date().toString());
            }
            super.handleMessage(msg);
        }
    }

    private static class ListenServer extends AsyncTask<String, Integer, String[]> {

        private AsyncTaskDone asyncTaskDone;

        ListenServer(AsyncTaskDone asyncTaskDone) {
            this.asyncTaskDone = asyncTaskDone;
        }

        @Override
        protected String[] doInBackground(String... strings) {
            byte[] bytes = new HttpUtil().doPost(Config.SERVER_CONFIG_FILE);
            if (bytes == null) return null;
            String result = new String(bytes);
            Log.i("Remote", "Result is " + result);
            return result.split(",");
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings == null) {
                asyncTaskDone.onFailed();
                return;
            }

            if (hour != Integer.parseInt(strings[0]) || minute != Integer.parseInt(strings[1])) {

                hour = Integer.parseInt(strings[0]);
                minute = Integer.parseInt(strings[1]);

                //接口回调
                asyncTaskDone.onSucceed();

            } else asyncTaskDone.onFailed();
        }
    }
}

