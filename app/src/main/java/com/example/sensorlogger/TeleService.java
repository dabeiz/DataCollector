package com.example.sensorlogger;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TeleService extends Service {

    String TAG = "TeleService";

    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStateListener;
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
        String channelID = "TeleServiceChannelID";
        String channelName = "信号扫描";
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
        builder.setContentText("正在持续扫描信号...");  // 通知内容
        // builder.setAutoCancel(true); // 用户点击这个通知时, 关闭通知... 但实际测试好像没用
        builder.setOngoing(true);  // 设置为一个正在进行的通知，此时用户无法清除通知
        // builder.setWhen(System.currentTimeMillis());  // 设定通知显示的时间, 默认为系统发出通知的时间
        // 还可以设置震动等提醒方式

        // 如果需要设置点击该通知后的动作, 可以在这里加入Intent和PendingIntent

        // 将服务置于启动状态, 第一个参数只要不是0就可以
        startForeground(3, builder.build());
    }

    @Override
    public void onCreate() {
        // service被创建时被调用
        super.onCreate();

        // 实例化tele管理器 及 手机状态监听器
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new MyPhoneStateListener();

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

        Log.d(TAG, "Listening Tele");
        mDataRecorder.recordData(startTimeMilis + "_TELE", "Start record at " + SDF.format(date) + "\n");

        // TelephonyManager本身有很多属性, 可以做为收集的信息. 这里只列举了少数几个.
        StringBuilder strTelephonyManager = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            strTelephonyManager.append("No READ_PHONE_STATE permission.\n");
        } else {
            strTelephonyManager.append("getNetworkType: ");
            strTelephonyManager.append(mTelephonyManager.getNetworkType());
            strTelephonyManager.append(", getDataState: ");
            strTelephonyManager.append(mTelephonyManager.getDataState());
            strTelephonyManager.append(", getDataActivity: ");
            strTelephonyManager.append(mTelephonyManager.getDataActivity());
            strTelephonyManager.append("\n");
        }
        mDataRecorder.recordData(startTimeMilis + "_TELE", "TelephonyManager: " + strTelephonyManager.toString());

        // 让管理器开始监听信号强度
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // service被关闭前被调用
        super.onDestroy();

        // 让管理器取消监听
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        stopForeground(true);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            /*  验证发现原本的 SignalStrength.toString() 就包含了这些信息
            try {
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteSignalStrength").invoke(signalStrength));
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteLevel").invoke(signalStrength));
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteRsrp").invoke(signalStrength));
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength));
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength));
                Log.d(TAG, "-----" + signalStrength.getClass().getMethod("getLteCqi").invoke(signalStrength));

            } catch (Exception e) {
                e.printStackTrace();
            }
            */

            /*  具体 SignalStrength.toString() 当中的每一项代表什么, 可以参见如下网址
            // https://stackoverflow.com/questions/5545026/how-to-get-lte-signal-strength-in-android#
            // 但是从我的输出来看, 好像还有几项是没有解释的
            // 一个样例输出: SignalStrength: 0 2066955648 -67 -73 32767 -1 32767 -1 -103 -11 -1 -1 0 2147483647 0 120 2147483647 255 gsm|lte use_rsrp_and_rssnr_for_lte_level rscp [-128, -118, -108, -98] [-115, -105, -95, -85] 0 0 0 0 0 0
            String strSignalStrength = "getlevel: " + signalStrength.getLevel()
                    + ", getGsmSignalStrength: " + signalStrength.getGsmSignalStrength()
                    + ", getGsmBitErrorRate: " + signalStrength.getGsmBitErrorRate()
                    + ", getCdmaDbm: " + signalStrength.getCdmaDbm()
                    + ", getCdmaEcio: " + signalStrength.getCdmaEcio()
                    + ", getEvdoDbm: " + signalStrength.getEvdoDbm()
                    + ", getEvdoEcio: " + signalStrength.getEvdoEcio()
                    + ", getEvdoSnr: " + signalStrength.getEvdoSnr();
            */
            mDataRecorder.recordData(startTimeMilis + "_TELE", System.currentTimeMillis() + "\t" + signalStrength.toString());

        }
    }

}
