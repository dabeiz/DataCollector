package com.example.sensorlogger;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MediaService extends Service implements SurfaceHolder.Callback {

    String TAG = "MediaService";

    boolean isVideo = false;
    boolean isAudio = false;

    private WindowManager mWindowManager;
    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder = null;
    private Camera mCamera = null;
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
        String channelID = "MediaServiceChannelID";
        String channelName = "多媒体录制";
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
        builder.setContentText("正在持续多媒体录制...");  // 通知内容
        // builder.setAutoCancel(true); // 用户点击这个通知时, 关闭通知... 但实际测试好像没用
        builder.setOngoing(true);  // 设置为一个正在进行的通知，此时用户无法清除通知
        // builder.setWhen(System.currentTimeMillis());  // 设定通知显示的时间, 默认为系统发出通知的时间
        // 还可以设置震动等提醒方式

        // 如果需要设置点击该通知后的动作, 可以在这里加入Intent和PendingIntent

        // 将服务置于启动状态, 第一个参数只要不是0就可以
        startForeground(6, builder.build());
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "executing onCreate()");

        // service被创建时被调用
        super.onCreate();

        // 整个系统只能有一个MediaRecorder
        mMediaRecorder = new MediaRecorder();

        // 实例化 数据记录器
        mDataRecorder = new DataRecorder();

        // 启动前台通知服务
        if (Build.VERSION.SDK_INT >= 26) {
            myStartForeground();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "executing onStartCommand()");
        // service被启动时被调用, 在客户端使用startService()可以调用这个函数, 返回值为int型, 代表不同状态

        startTimeMilis = intent.getLongExtra("Start", 0);
        Date date = new Date(startTimeMilis);

        Log.d(TAG, "Listening Media");

        // 如果用户只选择了 AUDIO, 那么只录制音频; 如果用户选择了VIDEO 或者同时选择了VIDEO和AUDIO, 那么只会录制视频
        isVideo = intent.getBooleanExtra("Video", false);
        isAudio = !isVideo && intent.getBooleanExtra("Audio", false);

        mMediaRecorder.reset();

        if (isVideo) {
            mDataRecorder.recordData(startTimeMilis + "_VIDEO", "Start record at " + SDF.format(date) + "\n");

            // 创建一个1x1大小的dummy surfaceView, 并且将这个服务设置为回调
            mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            mSurfaceView  = new SurfaceView(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );

            layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            mWindowManager.addView(mSurfaceView, layoutParams);

            // 这里callback是指使用SurfaceHolder.Callback 里的三个监听(回调)函数
            // 比如 surfaceview 完成之后自动调用 onSurfaceCreated()
            mSurfaceView.getHolder().addCallback(this);
        }


        if (isAudio) {
            mDataRecorder.recordData(startTimeMilis + "_AUDIO", "Start record at " + SDF.format(date) + "\n");

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/mylogger/" + startTimeMilis + "_AUDIO.amr");
            try { mMediaRecorder.prepare(); } catch (Exception e) { e.printStackTrace(); }
            mMediaRecorder.start();
        }

        return START_STICKY;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        Log.d(TAG, "surfaceCreated!, got startMilis: " + startTimeMilis);

        // 开始准备录制, 拍摄视频的这些API函数调用需要严格按照顺序

        // 步骤1: 创建相机示例, 解锁相机, 并且将相机配置到多媒体记录器
        mCamera = Camera.open();  // 可以是 .open(int)用来规定开哪个摄像头
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // 步骤2: 设置多媒体源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // 步骤3: 设置视频的输出和编码格式, 在API8以上可以直接调用setProfile方法进行相关配置
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // 步骤4: 设置输出文件
        mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/mylogger/" + startTimeMilis + "_VIDEO.mp4");

        // 步骤5: 设置捕获视频图像的预览界面
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        // 步骤6: 准备配置好的多媒体记录器 并开始
        try { mMediaRecorder.prepare(); } catch (Exception e) { e.printStackTrace(); }
        mMediaRecorder.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // 实现 SurfaceHolder.Callback必须重写
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // 实现 SurfaceHolder.Callback必须重写
    }

    @Override
    public void onDestroy() {

        // service被关闭前被调用
        super.onDestroy();

        // 停止多媒体录制并释放资源
        mMediaRecorder.stop();
        mMediaRecorder.reset();   // clear recorder configuration
        mMediaRecorder.release(); // release the recorder object
        mMediaRecorder = null;

        if (isVideo) {
            mCamera.lock();           // lock camera for later use
            mCamera.release();
            mWindowManager.removeView(mSurfaceView);
        }

        stopForeground(true);
    }
}
