package com.example.sensorlogger;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class WifiService extends Service {

    String TAG = "WifiService";

    private WifiManager mWifiManager;
    private DataRecorder mDataRecorder;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    long startTimeMilis = 0;
    SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Override
    public IBinder onBind(Intent intent) {
        // Service子类必须实现, 绑定service时被调用
        return null;
    }


    @TargetApi(26)
    private void myStartForeground() {

        // 设定通知channel的 ID和名称以及重要程度
        String channelID = "WifiServiceChannelID";
        String channelName = "WIFI扫描";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        // 构建通知channel
        NotificationChannel mNotificationChannel = new NotificationChannel(channelID, channelName, importance);
        // 指定用户在系统设置页面看到的关于通知渠道的相关描述, 还可以设置LED灯, 小红点或者是否震动提醒等功能
        // mNotificationChannel.setDescription("Wifi Service Channel description");

        // 向系统注册通知channel，注册后不能改变重要性以及其他通知行为
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        // 在创建的通知渠道上发送通知, 以及设置同时的样式和属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);  // 通知小图标 (可以自己在/res/文件加上)
        builder.setContentTitle("mylogger");  // 通知标题
        builder.setContentText("正在持续扫描WIFI...");  // 通知内容
        // builder.setAutoCancel(true); // 用户点击这个通知时, 关闭通知... 但实际测试好像没用
        builder.setOngoing(true);  // 设置为一个正在进行的通知，此时用户无法清除通知
        // builder.setWhen(System.currentTimeMillis());  // 设定通知显示的时间, 默认为系统发出通知的时间
        // 还可以设置震动等提醒方式

        // 如果需要设置点击该通知后的动作, 可以在这里加入Intent和PendingIntent

        // 将服务置于启动状态, 第一个参数只要不是0就可以
        startForeground(2, builder.build());
    }


    @Override
    public void onCreate() {
        // service被创建时被调用
        super.onCreate();

        // 实例化 wifi管理器
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 实例化 数据记录器
        mDataRecorder = new DataRecorder();

        // 启动前台通知服务
        if (Build.VERSION.SDK_INT >= 26) {
            myStartForeground();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // service被启动时被调用, 在客户端使用startService()可以调用这个函数, 返回值为int型, 代表不同状态

        startTimeMilis = intent.getLongExtra("Start", 0);
        Date date = new Date(startTimeMilis);

        Log.d(TAG, "Listening Wifi");
        mDataRecorder.recordData(startTimeMilis + "_WIFI", "Start record at " + SDF.format(date) + "\n");

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currTimeMilis = System.currentTimeMillis();
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

                // 记录当前连接的wifi信息
                String strWifiInfo = "SSID: " + wifiInfo.getBSSID() +
                                     ", BSSID: " + wifiInfo.getBSSID() +
                                     ", Supplicant state: " + wifiInfo.getSupplicantState() +
                                     ", RSSI: " + wifiInfo.getRssi() +
                                     ", Link speed: " + wifiInfo.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS +
                                     ", Frequency: " + wifiInfo.getFrequency() + WifiInfo.FREQUENCY_UNITS +
                                     ", Net ID: " + wifiInfo.getNetworkId() + "\n";

                // 记录扫描得到的所有wifi信息
                StringBuilder listInfo = new StringBuilder();
                for (ScanResult scanResult:scanResults) {
                    listInfo.append(scanResult.toString());
                    listInfo.append("\n");
                }

                mDataRecorder.recordData(startTimeMilis + "_WIFI", currTimeMilis + " getConnectionInfo: \n" + strWifiInfo);
                mDataRecorder.recordData(startTimeMilis + "_WIFI", currTimeMilis + " getScanResults: \n" + listInfo.toString());
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // service被关闭前被调用
        super.onDestroy();

        stopForeground(true);

        //关闭scheduler
        scheduler.shutdown();
    }
}
