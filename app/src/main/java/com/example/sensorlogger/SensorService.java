package com.example.sensorlogger;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SensorService extends Service implements SensorEventListener{

    private SensorManager mSensorManager;
    private DataRecorder mDataRecorder;
    String TAG = "SensorService";
    long startTimeMilis = 0;
    SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // 控件选择状态的bool型变量的声明
    boolean checkedAccelerometer = false;
    boolean checkedAccelerometerUncalibrated = false;
    boolean checkedAmbientTemperature= false;
    boolean checkedGameRotationVector = false;
    boolean checkedGeomagneticRotationVector = false;
    boolean checkedGravity = false;
    boolean checkedGyroscope = false;
    boolean checkedGyroscopeUncalibrated = false;
    boolean checkedLight = false;
    boolean checkedLinearAcceleration = false;
    boolean checkedMagneticField = false;
    boolean checkedMagneticFieldUncalibrated = false;
    boolean checkedPressure = false;
    boolean checkedProximity = false;
    boolean checkedRelativeHumidity = false;
    boolean checkedRotationVector = false;
    boolean checkedStepCounter = false;


    @Override
    public IBinder onBind(Intent intent) {
        // Service子类必须实现, 绑定service时被调用
        return null;
    }

    @TargetApi(26)
    private void myStartForeground() {

        // 设定通知channel的 ID和名称以及重要程度
        String channelID = "SensorServiceChannelID";
        String channelName = "传感器监听";
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
        builder.setContentText("正在持续监听传感器...");  // 通知内容
        // builder.setAutoCancel(true); // 用户点击这个通知时, 关闭通知... 但实际测试好像没用
        builder.setOngoing(true);  // 设置为一个正在进行的通知，此时用户无法清除通知
        // builder.setWhen(System.currentTimeMillis());  // 设定通知显示的时间, 默认为系统发出通知的时间
        // 还可以设置震动等提醒方式

        // 如果需要设置点击该通知后的动作, 可以在这里加入Intent和PendingIntent

        // 将服务置于启动状态, 第一个参数只要不是0就可以
        startForeground(7, builder.build());
    }

    @Override
    public void onCreate() {
        // service被创建时被调用
        super.onCreate();
        // 实例化 传感器管理
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //实例化 数据记录器
        mDataRecorder = new DataRecorder();

        // 启动前台通知服务
        if (Build.VERSION.SDK_INT >= 26) {
            myStartForeground();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // service被启动时被调用, 在客户端使用startService()可以调用这个函数, 返回值为int型, 代表不同状态

        // 每次按下start按钮后, 开启一批新文件用来记录, 文件名前缀为当前系统时间.
        startTimeMilis = intent.getLongExtra("Start", 0);
        Date date = new Date(startTimeMilis);

        checkedAccelerometer = intent.getBooleanExtra("Accelerometer", false);
        checkedAccelerometerUncalibrated = intent.getBooleanExtra("AccelerometerUncalibrated", false);
        checkedAmbientTemperature = intent.getBooleanExtra("AmbientTemperature", false);
        checkedGameRotationVector = intent.getBooleanExtra("GameRotationVector", false);
        checkedGeomagneticRotationVector = intent.getBooleanExtra("GeomagneticRotationVector", false);
        checkedGravity = intent.getBooleanExtra("Gravity", false);
        checkedGyroscope = intent.getBooleanExtra("Gyroscope", false);
        checkedGyroscopeUncalibrated = intent.getBooleanExtra("GyroscopeUncalibrated", false);
        checkedLight = intent.getBooleanExtra("Light", false);
        checkedLinearAcceleration = intent.getBooleanExtra("LinearAcceleration", false);
        checkedMagneticField = intent.getBooleanExtra("MagneticField", false);
        checkedMagneticFieldUncalibrated = intent.getBooleanExtra("MagneticFieldUncalibrated", false);
        checkedPressure= intent.getBooleanExtra("Pressure", false);
        checkedProximity= intent.getBooleanExtra("Proximity", false);
        checkedRelativeHumidity= intent.getBooleanExtra("RelativeHumidity", false);
        checkedRotationVector= intent.getBooleanExtra("RotationVector", false);
        checkedStepCounter= intent.getBooleanExtra("StepCounter", false);

        // 函数 SensorManager.registerListener()
        // 注册传感器并让本类实现SensorEventListener接口, 可以注册让SensorManage同时管理多个传感器实例
        //      第一个参数: SensorEventListener 接口的实例对象
        //      第二个参数: Sensor 需要注册的传感器实例
        //      第三个参数: SamplingPeriodUS传感器获取传感器事件event值频率, 可以自定义int, 单位微秒

        if (checkedAccelerometer) {
            Log.d(TAG, "Listening Accelerometer");
            mDataRecorder.recordData(startTimeMilis + "_ACCELEROMETER", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedAccelerometerUncalibrated) {
            Log.d(TAG, "Listening AccelerometerUncalibrated");
            mDataRecorder.recordData(startTimeMilis + "_ACCELEROMETER_UNCALIBRATED", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedAmbientTemperature) {
            Log.d(TAG, "Listening AmbientTemperature");
            mDataRecorder.recordData(startTimeMilis + "_AMBIENT_TEMPERATURE", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedGameRotationVector) {
            Log.d(TAG, "Listening GameRotationVector");
            mDataRecorder.recordData(startTimeMilis + "_GAME_ROTATION_VECTOR", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedGeomagneticRotationVector) {
            Log.d(TAG, "Listening GeomagneticRotationVector");
            mDataRecorder.recordData(startTimeMilis + "_GEOMAGNETIC_ROTATION_VECTOR", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedGravity) {
            Log.d(TAG, "Listening Gravity");
            mDataRecorder.recordData(startTimeMilis + "_GRAVITY", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedGyroscope) {
            Log.d(TAG, "Listening Gyroscope");
            mDataRecorder.recordData(startTimeMilis + "_GYROSCOPE", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedGyroscopeUncalibrated) {
            Log.d(TAG, "Listening GyroscopeUncalibrated");
            mDataRecorder.recordData(startTimeMilis + "_GYROSCOPE_UNCALIBRATED", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedLight) {
            Log.d(TAG, "Listening Light");
            mDataRecorder.recordData(startTimeMilis + "_LIGHT", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedLinearAcceleration) {
            Log.d(TAG, "Listening LinearAcceleration");
            mDataRecorder.recordData(startTimeMilis + "_LINEAR_ACCELERATION", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedMagneticField) {
            Log.d(TAG, "Listening MagneticField");
            mDataRecorder.recordData(startTimeMilis + "_MAGNETIC_FIELD", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedMagneticFieldUncalibrated) {
            Log.d(TAG, "Listening MagneticFieldUncalibrated");
            mDataRecorder.recordData(startTimeMilis + "_MAGNETIC_FIELD_UNCALIBRATED", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedPressure) {
            Log.d(TAG, "Listening Pressure");
            mDataRecorder.recordData(startTimeMilis + "_PRESSURE", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedProximity) {
            Log.d(TAG, "Listening Proximity");
            mDataRecorder.recordData(startTimeMilis + "_PROXIMITY", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedRelativeHumidity) {
            Log.d(TAG, "Listening RelativeHumidity");
            mDataRecorder.recordData(startTimeMilis + "_RELATIVE_HUMIDITY", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedRotationVector) {
            Log.d(TAG, "Listening RotationVector");
            mDataRecorder.recordData(startTimeMilis + "_ROTATION_VECTOR", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (checkedStepCounter) {
            Log.d(TAG, "Listening StepCounter");
            mDataRecorder.recordData(startTimeMilis + "_STEP_COUNTER", "Start record at " + SDF.format(date) + "\n");
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_FASTEST);
        }

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 实现SensorEventListener 必须重写onAccuracyChanged()
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // 实现SensorEventListener 必须重写onSensorChanged()
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mDataRecorder.recordData(startTimeMilis + "_ACCELEROMETER", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                mDataRecorder.recordData(startTimeMilis + "_ACCELEROMETER_UNCALIBRATED", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mDataRecorder.recordData(startTimeMilis + "_AMBIENT_TEMPERATURE", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                mDataRecorder.recordData(startTimeMilis + "_GAME_ROTATION_VECTOR", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                mDataRecorder.recordData(startTimeMilis + "_GEOMAGNETIC_ROTATION_VECTOR", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_GRAVITY:
                mDataRecorder.recordData(startTimeMilis + "_GRAVITY", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_GYROSCOPE:
                mDataRecorder.recordData(startTimeMilis + "_GYROSCOPE", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                mDataRecorder.recordData(startTimeMilis + "_GYROSCOPE_UNCALIBRATED", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_LIGHT:
                mDataRecorder.recordData(startTimeMilis + "_LIGHT", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                mDataRecorder.recordData(startTimeMilis + "_LINEAR_ACCELERATION", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mDataRecorder.recordData(startTimeMilis + "_MAGNETIC_FIELD", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                mDataRecorder.recordData(startTimeMilis + "_MAGNETIC_FIELD_UNCALIBRATED", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_PRESSURE:
                mDataRecorder.recordData(startTimeMilis + "_PRESSURE", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_PROXIMITY:
                mDataRecorder.recordData(startTimeMilis + "_PROXIMITY", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                mDataRecorder.recordData(startTimeMilis + "_RELATIVE_HUMIDITY", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                mDataRecorder.recordData(startTimeMilis + "_ROTATION_VECTOR", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
            case Sensor.TYPE_STEP_COUNTER:
                mDataRecorder.recordData(startTimeMilis + "_STEP_COUNTER", event.timestamp + "\t" + formatData(event.values) + "\n");
                break;
        }
    }

    @Override
    public void onDestroy() {
        // service被关闭前被调用
        super.onDestroy();

        // 注销传感器监听器
        mSensorManager.unregisterListener(this);

        stopForeground(true);
    }

    private String formatData(float[] values) {
        StringBuilder result = new StringBuilder();
        for (float value : values) {
            result.append(value);
            result.append("\t");
        }
        return result.toString();
    }
}
