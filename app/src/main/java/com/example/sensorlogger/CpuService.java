package com.example.sensorlogger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CpuService extends Service {

    String TAG = "CpuService";

    private DataRecorder mDataRecorder;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    long startTimeMilis = 0;
    SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Override
    public IBinder onBind(Intent intent) {
        // Service子类必须实现, 绑定service时被调用
        return null;
    }

    @Override
    public void onCreate() {
        // service被创建时被调用
        super.onCreate();

        // 实例化 数据记录器
        mDataRecorder = new DataRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // service被启动时被调用, 在客户端使用startService()可以调用这个函数, 返回值为int型, 代表不同状态

        startTimeMilis = intent.getLongExtra("Start", 0);
        Date date = new Date(startTimeMilis);

        Log.d(TAG, "Listening Cpu");
        mDataRecorder.recordData(startTimeMilis + "_CPU", "Start record at " + SDF.format(date) + "\n");

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
                    String load = reader.readLine();
                    reader.close();
                    Log.d(TAG, System.currentTimeMillis() + "--" + load);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // service被关闭前被调用
        super.onDestroy();

        // 关闭schedule
        scheduler.shutdown();
    }
}

