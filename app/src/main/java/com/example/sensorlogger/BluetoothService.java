package com.example.sensorlogger;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BluetoothService extends Service {

    String TAG = "BluetoothService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private DataRecorder mDataRecorder;

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
        String channelID = "BluetoothServiceChannelID";
        String channelName = "蓝牙状态监听";
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
        builder.setContentText("正在持续监听蓝牙状态...");  // 通知内容
        // builder.setAutoCancel(true); // 用户点击这个通知时, 关闭通知... 但实际测试好像没用
        builder.setOngoing(true);  // 设置为一个正在进行的通知，此时用户无法清除通知
        // builder.setWhen(System.currentTimeMillis());  // 设定通知显示的时间, 默认为系统发出通知的时间
        // 还可以设置震动等提醒方式

        // 如果需要设置点击该通知后的动作, 可以在这里加入Intent和PendingIntent

        // 将服务置于启动状态, 第一个参数只要不是0就可以
        startForeground(5, builder.build());
    }

    @Override
    public void onCreate() {
        // service被创建时被调用
        super.onCreate();

        // 实例化蓝牙管理器
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

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

        Log.d(TAG, "Listening Bluetooth");
        mDataRecorder.recordData(startTimeMilis + "_BLUETOOTH", "Start record at " + SDF.format(date) + "\n");

        String strFormat = "Name\tState\tScanMode\n";
        mDataRecorder.recordData(startTimeMilis + "_BLUETOOTH", strFormat);

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            // Log.d(TAG, "Bluetooth not open!");
            mDataRecorder.recordData(startTimeMilis + "_BLUETOOTH", "Bluetooth not open!\n");
        } else if (mBluetoothAdapter == null) {
            // Log.d(TAG, "No Device.");
            mDataRecorder.recordData(startTimeMilis + "_BLUETOOTH", "No bluetooth in device!\n");
        } else {
            // Log.d(TAG, mBluetoothAdapter.getName() + "--" + mBluetoothAdapter.getState() + "--" + mBluetoothAdapter.getScanMode());
            mDataRecorder.recordData(startTimeMilis + "_BLUETOOTH", mBluetoothAdapter.getName() + "\t" + mBluetoothAdapter.getState() + "\t" + mBluetoothAdapter.getScanMode() + "\n");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // service被关闭前被调用
        super.onDestroy();

        stopForeground(true);
    }
}
