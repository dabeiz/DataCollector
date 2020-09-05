package com.example.sensorlogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// TODO: 将service全部改写为foregroundstart


// TODO: timestamp 与 System.currentTimeMillis() 的区别
// timestamp: 3573726995175  这是单位: ns, 表示我的手机到该event发生时的已开机时间!!!!
// System.currentTimeMillis(): 1597116615561  单位: ms, 表示从1970.1.1到调用这个函数时经过的时间

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private static String[] PERMISSIONS = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,  // 访问设备上的照片, 媒体内容和文件
        Manifest.permission.READ_PHONE_STATE,  // 获取设备信息(包括读取设备通话状态和识别码)
        Manifest.permission.ACCESS_FINE_LOCATION,  // 获取此设备的位置信息
        Manifest.permission.RECORD_AUDIO,  // 录制音频
        Manifest.permission.CAMERA,  // 拍摄照片和录制视频
    };

    // 辅助变量, 记录是否是真实地用户点击stop
    boolean forcedStop = false;

    ToggleButton tbStartStop;
    CheckBox cbScreen;
    CheckBox cbWifi;
    CheckBox cbTele;
    CheckBox cbLocation;
    CheckBox cbBluetooth;
    CheckBox cbAudio;
    CheckBox cbVideo;

    // 控件及记录其选择状态的bool型变量的声明
    boolean checkedScreen = false;
    boolean checkedWifi = false;
    boolean checkedTele = false;
    boolean checkedLocation = false;
    boolean checkedBluetooth = false;
    boolean checkedAudio = false;
    boolean checkedVideo = false;

    // boolean checkedCpu = false;
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


    Intent intentToSensorService = null;
    Intent intentToScreenService = null;
    Intent intentToWifiService = null;
    Intent intentToTeleService = null;
    Intent intentToLocationService = null;
    Intent intentToBluetoothService = null;
    Intent intentToMediaService = null;
    // Intent intentToCpuService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");


        // 控件, 相应监听, 及记录其选择状态的bool型变量的实例化
        // BtListener btListener = new BtListener();
        CbListener cbListener = new CbListener();
        TbListener tbListener = new TbListener();

        // 原始start/stop
        // Button btStart = findViewById(R.id.btStart);
        // btStart.setOnClickListener(btListener);
        // Button btStop = findViewById(R.id.btStop);
        // btStop.setOnClickListener(btListener);

        tbStartStop = findViewById(R.id.tbStartStop);
        tbStartStop.setOnCheckedChangeListener(tbListener);

        cbScreen = findViewById(R.id.cbSCREEN);
        cbScreen.setOnCheckedChangeListener(cbListener);

        cbWifi = findViewById(R.id.cbWIFI);
        cbWifi.setOnCheckedChangeListener(cbListener);

        cbTele = findViewById(R.id.cbTELE);
        cbTele.setOnCheckedChangeListener(cbListener);

        cbLocation = findViewById(R.id.cbLOCATION);
        cbLocation.setOnCheckedChangeListener(cbListener);

        cbBluetooth = findViewById(R.id.cbBLUETOOTH);
        cbBluetooth.setOnCheckedChangeListener(cbListener);

        cbAudio = findViewById(R.id.cbAUDIO);
        cbAudio.setOnCheckedChangeListener(cbListener);

        cbVideo = findViewById(R.id.cbVIDEO);
        cbVideo.setOnCheckedChangeListener(cbListener);

        // 因为无法将app升级为系统app, 因此无法使用此功能
        // CheckBox cbCpu = findViewById(R.id.cbCPU);
        // cbCpu.setEnabled(false);
        // cbCpu.setOnCheckedChangeListener(cbListener);


        // 为所有sensor指定监听器

        CheckBox cbAccelerometer = findViewById(R.id.cbACCELEROMETER);
        cbAccelerometer.setOnCheckedChangeListener(cbListener);

        CheckBox cbAccelerometerUncalibrated = findViewById(R.id.cbACCELEROMETER_UNCALIBRATED);
        cbAccelerometerUncalibrated.setOnCheckedChangeListener(cbListener);

        CheckBox cbAmbientTemperature = findViewById(R.id.cbAMBIENT_TEMPERATURE);
        cbAmbientTemperature.setOnCheckedChangeListener(cbListener);

        CheckBox cbGameRotationVector = findViewById(R.id.cbGAME_ROTATION_VECTOR);
        cbGameRotationVector.setOnCheckedChangeListener(cbListener);

        CheckBox cbGeomagneticRotationVector = findViewById(R.id.cbGEOMAGNETIC_ROTATION_VECTOR);
        cbGeomagneticRotationVector.setOnCheckedChangeListener(cbListener);

        CheckBox cbGravity = findViewById(R.id.cbGRAVITY);
        cbGravity.setOnCheckedChangeListener(cbListener);

        CheckBox cbGyroscope = findViewById(R.id.cbGYROSCOPE);
        cbGyroscope.setOnCheckedChangeListener(cbListener);

        CheckBox cbGyroscopeUncalibrated = findViewById(R.id.cbGYROSCOPE_UNCALIBRATED);
        cbGyroscopeUncalibrated.setOnCheckedChangeListener(cbListener);

        CheckBox cbLight = findViewById(R.id.cbLIGHT);
        cbLight.setOnCheckedChangeListener(cbListener);

        CheckBox cbLinearAcceleration = findViewById(R.id.cbLINEAR_ACCELERATION);
        cbLinearAcceleration.setOnCheckedChangeListener(cbListener);

        CheckBox cbMagneticField = findViewById(R.id.cbMAGNETIC_FIELD);
        cbMagneticField.setOnCheckedChangeListener(cbListener);

        CheckBox cbMagneticFieldUncalibrated = findViewById(R.id.cbMAGNETIC_FIELD_UNCALIBRATED);
        cbMagneticFieldUncalibrated.setOnCheckedChangeListener(cbListener);

        CheckBox cbPressure = findViewById(R.id.cbPRESSURE);
        cbPressure.setOnCheckedChangeListener(cbListener);

        CheckBox cbProximity= findViewById(R.id.cbPROXIMITY);
        cbProximity.setOnCheckedChangeListener(cbListener);

        CheckBox cbRelativeHumidity = findViewById(R.id.cbRELATIVE_HUMIDITY);
        cbRelativeHumidity.setOnCheckedChangeListener(cbListener);

        CheckBox cbRotaionVector = findViewById(R.id.cbROTATION_VECTOR);
        cbRotaionVector.setOnCheckedChangeListener(cbListener);

        CheckBox cbStepCounter = findViewById(R.id.cbSTEP_COUNTER);
        cbStepCounter.setOnCheckedChangeListener(cbListener);

        // 检查所有可用的传感器
        SensorManager mSensorManager;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i=0; i<deviceSensors.size(); i++) {
            switch (deviceSensors.get(i).getType()) {
                case 1: cbAccelerometer.setEnabled(true); break;
                case 35: cbAccelerometerUncalibrated.setEnabled(true); break;
                case 13: cbAmbientTemperature.setEnabled(true); break;
                case 15: cbGameRotationVector.setEnabled(true); break;
                case 20: cbGeomagneticRotationVector.setEnabled(true); break;
                case 9: cbGravity.setEnabled(true); break;
                case 4: cbGyroscope.setEnabled(true); break;
                case 16: cbGyroscopeUncalibrated.setEnabled(true); break;
                case 5: cbLight.setEnabled(true); break;
                case 10: cbLinearAcceleration.setEnabled(true); break;
                case 2: cbMagneticField.setEnabled(true); break;
                case 14: cbMagneticFieldUncalibrated.setEnabled(true); break;
                case 6: cbPressure.setEnabled(true); break;
                case 8: cbProximity.setEnabled(true); break;
                case 12: cbRelativeHumidity.setEnabled(true); break;
                case 11: cbRotaionVector.setEnabled(true); break;
                case 19: cbStepCounter.setEnabled(true); break;
            }
        }
    }


    class TbListener implements CompoundButton.OnCheckedChangeListener{
        // 监听用户的Strat/Stop选择
        // 注意这里可能也会监听到授予权限被denied后由程序发起的 setChecked()
        @Override
        public void onCheckedChanged(CompoundButton compoundButtonm, boolean isChecked) {
            compoundButtonm.setChecked(isChecked);
            if (isChecked) {  // 表示用户想要开始记录

                // 每次用户开始选择start都初始化这个值
                forcedStop = false;

                // 所有记录都需要用户授权外存读写权限, 因此在start按钮这提出比较合适
                askForPermissions(MainActivity.this, R.id.tbStartStop);
            }

            else {  // 表示用户想要停止记录, 但如果是用户点了start但没有授权后被此程序强制stop, 则不执行任何操作
                if (!forcedStop) {
                    stopEverything();
                }
            }
        }
    }


    public void startEverything() {
        // 点击start按钮且用户同意授予外存读写权限的后续操作

        long startTimeMilis = System.currentTimeMillis();
        Log.d(TAG, "StartTimeMilis is: " + startTimeMilis);

        if (checkedScreen) {
            intentToScreenService = new Intent(MainActivity.this, ScreenService.class);
            intentToScreenService.putExtra("Start", startTimeMilis);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToScreenService);
            } else {
                startService(intentToScreenService);
            }
        }

        if (checkedWifi) {
            intentToWifiService = new Intent(MainActivity.this, WifiService.class);
            intentToWifiService.putExtra("Start", startTimeMilis);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToWifiService);
            } else {
                startService(intentToWifiService);
            }
        }

        if (checkedTele) {
            intentToTeleService = new Intent(MainActivity.this, TeleService.class);
            intentToTeleService.putExtra("Start", startTimeMilis);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToTeleService);
            } else {
                startService(intentToTeleService);
            }
        }

        if (checkedLocation) {
            intentToLocationService = new Intent(MainActivity.this, LocationService.class);
            intentToLocationService.putExtra("Start", startTimeMilis);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToLocationService);
            } else {
                startService(intentToLocationService);
            }
        }

        if (checkedBluetooth) {
            intentToBluetoothService = new Intent(MainActivity.this, BluetoothService.class);
            intentToBluetoothService.putExtra("Start", startTimeMilis);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToBluetoothService);
            } else {
                startService(intentToBluetoothService);
            }
        }

        /* 因为无法将app升级为系统app, 因此无法使用此功能
        if (checkedCpu) {
            intentToCpuService = new Intent(MainActivity.this, CpuService.class);
            intentToCpuService.putExtra("Start", startTimeMilis);
            startService(intentToCpuService);
        } */

        if (checkedAudio || checkedVideo) {
            intentToMediaService = new Intent(MainActivity.this, MediaService.class);
            intentToMediaService.putExtra("Start", startTimeMilis);
            intentToMediaService.putExtra("Audio", checkedAudio);
            intentToMediaService.putExtra("Video", checkedVideo);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToMediaService);
            } else {
                startService(intentToMediaService);
            }
        }

        if (checkedAccelerometer || checkedAccelerometerUncalibrated || checkedAmbientTemperature
                || checkedGameRotationVector || checkedGeomagneticRotationVector || checkedGravity || checkedGyroscope
                || checkedGyroscopeUncalibrated || checkedLight || checkedLinearAcceleration || checkedMagneticField
                || checkedMagneticFieldUncalibrated || checkedPressure || checkedProximity
                || checkedRelativeHumidity || checkedRotationVector || checkedStepCounter) {

            intentToSensorService = new Intent(MainActivity.this, SensorService.class);
            intentToSensorService.putExtra("Start", startTimeMilis);

            intentToSensorService.putExtra("Accelerometer", checkedAccelerometer);
            intentToSensorService.putExtra("AccelerometerUncalibrated", checkedAccelerometerUncalibrated);
            intentToSensorService.putExtra("AmbientTemperature", checkedAmbientTemperature);
            intentToSensorService.putExtra("GameRotationVector", checkedGameRotationVector);
            intentToSensorService.putExtra("GeomagneticRotationVector", checkedGeomagneticRotationVector);
            intentToSensorService.putExtra("Gravity", checkedGravity);
            intentToSensorService.putExtra("Gyroscope", checkedGyroscope);
            intentToSensorService.putExtra("GyroscopeUncalibrated", checkedGyroscopeUncalibrated);
            intentToSensorService.putExtra("Light", checkedLight);
            intentToSensorService.putExtra("LinearAcceleration", checkedLinearAcceleration);
            intentToSensorService.putExtra("MagneticField", checkedMagneticField);
            intentToSensorService.putExtra("MagneticFieldUncalibrated", checkedMagneticFieldUncalibrated);
            intentToSensorService.putExtra("Pressure", checkedPressure);
            intentToSensorService.putExtra("Proximity", checkedProximity);
            intentToSensorService.putExtra("RelativeHumidity", checkedRelativeHumidity);
            intentToSensorService.putExtra("RotationVector", checkedRotationVector);
            intentToSensorService.putExtra("RotationVector", checkedRotationVector);

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentToSensorService);
            } else {
                startService(intentToSensorService);
            }
        }
    }


    public void stopEverything() {
        // 真正的用户点击stop按钮的后续操作

        long stopTimeMilis = System.currentTimeMillis();
        Log.d(TAG, "StopTimeMilis is: " + stopTimeMilis);

        if (checkedScreen) {
            stopService(intentToScreenService);
        }

        if (checkedWifi) {
            stopService(intentToWifiService);
        }

        if (checkedTele) {
            stopService(intentToTeleService);
        }

        if (checkedLocation) {
            stopService(intentToLocationService);
        }

        if (checkedBluetooth) {
            stopService(intentToBluetoothService);
        }

        if (checkedAudio || checkedVideo) {
            stopService(intentToMediaService);
        }

        /*  因为无法将app升级为系统app, 因此无法使用此功能
        if (checkedCpu) {
            stopService(intentToCpuService);
        } */

        if (checkedAccelerometer || checkedAccelerometerUncalibrated || checkedAmbientTemperature
                || checkedGameRotationVector || checkedGeomagneticRotationVector || checkedGravity || checkedGyroscope
                || checkedGyroscopeUncalibrated || checkedLight || checkedLinearAcceleration || checkedMagneticField
                || checkedMagneticFieldUncalibrated || checkedPressure || checkedProximity
                || checkedRelativeHumidity || checkedRotationVector || checkedStepCounter) {
            stopService(intentToSensorService);
        }
    }


    class CbListener implements CompoundButton.OnCheckedChangeListener {
        // 记录各个 CheckBox 的选择

        @Override
        public void onCheckedChanged(CompoundButton compoundButtonm, boolean isChecked) {

            switch (compoundButtonm.getId()) {
                case R.id.cbSCREEN:
                    checkedScreen = isChecked; break;
                case R.id.cbWIFI:
                    checkedWifi = isChecked;  // 取消某个选项可以直接取消
                    if (isChecked) {  // 要勾选某个选项则需要判断是否有权限
                        askForPermissions(MainActivity.this, R.id.cbWIFI);
                    } break;
                case R.id.cbTELE:
                    checkedTele = isChecked;
                    if (isChecked) {
                        askForPermissions(MainActivity.this, R.id.cbTELE);
                    } break;
                case R.id.cbLOCATION:
                    checkedLocation = isChecked;
                    if (isChecked) {
                        askForPermissions(MainActivity.this, R.id.cbLOCATION);
                    } break;
                case R.id.cbBLUETOOTH:
                    checkedBluetooth = isChecked;
                    if (isChecked) {
                        askForPermissions(MainActivity.this, R.id.cbBLUETOOTH);
                    } break;
                case R.id.cbAUDIO:
                    checkedAudio = isChecked;
                    if (isChecked) {
                        askForPermissions(MainActivity.this, R.id.cbAUDIO);
                    } break;
                case R.id.cbVIDEO:
                    checkedVideo = isChecked;
                    if (isChecked) {
                        askForPermissions(MainActivity.this, R.id.cbVIDEO);
                    } break;

                // 因为无法将app升级为系统app, 因此无法使用此功能
                // case R.id.cbCPU:
                //    checkedCpu = isChecked; break;

                case R.id.cbACCELEROMETER:
                    checkedAccelerometer = isChecked; break;
                case R.id.cbACCELEROMETER_UNCALIBRATED:
                    checkedAccelerometerUncalibrated = isChecked; break;
                case R.id.cbAMBIENT_TEMPERATURE:
                    checkedAmbientTemperature = isChecked; break;
                case R.id.cbGAME_ROTATION_VECTOR:
                    checkedGameRotationVector = isChecked; break;
                case R.id.cbGEOMAGNETIC_ROTATION_VECTOR:
                    checkedGeomagneticRotationVector = isChecked; break;
                case R.id.cbGRAVITY:
                    checkedGravity = isChecked; break;
                case R.id.cbGYROSCOPE:
                    checkedGyroscope = isChecked; break;
                case R.id.cbGYROSCOPE_UNCALIBRATED:
                    checkedGyroscopeUncalibrated = isChecked; break;
                case R.id.cbLIGHT:
                    checkedLight = isChecked; break;
                case R.id.cbLINEAR_ACCELERATION:
                    checkedLinearAcceleration = isChecked; break;
                case R.id.cbMAGNETIC_FIELD:
                    checkedMagneticField = isChecked; break;
                case R.id.cbMAGNETIC_FIELD_UNCALIBRATED:
                    checkedMagneticFieldUncalibrated = isChecked; break;
                case R.id.cbPRESSURE:
                    checkedPressure = isChecked; break;
                case R.id.cbPROXIMITY:
                    checkedProximity = isChecked; break;
                case R.id.cbRELATIVE_HUMIDITY:
                    checkedRelativeHumidity = isChecked; break;
                case R.id.cbROTATION_VECTOR:
                    checkedRotationVector = isChecked; break;
                case R.id.cbSTEP_COUNTER:
                    checkedStepCounter = isChecked; break;
            }
        }
    }


    public void askForPermissions(Activity activity, int buttonID) {
        // 一个选项被选择需要满足两个条件:
        // 1. 相应的权限Permission被打开
        // 2. 相应的服务被打开

        // 可以不需要LocationManager也能判断是否开启位置服务
        boolean locationOFF = true;
        try { locationOFF = (Settings.Secure.LOCATION_MODE_OFF == Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE));
        } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
        Intent intentToLocationON = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        switch (buttonID) {
            case R.id.tbStartStop:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
                break;
            case R.id.cbWIFI:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if (locationOFF) {  // 如果用户没有开启位置服务, 则引导跳转至设置界面
                    startActivityForResult(intentToLocationON, 8);
                }
                break;
            case R.id.cbTELE:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_PHONE_STATE}, 2);
                break;
            case R.id.cbLOCATION:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 3);
                if (locationOFF) {  // 如果用户没有开启位置服务, 则引导跳转至设置界面
                    startActivityForResult(intentToLocationON, 9);
                }
                break;
            case R.id.cbBLUETOOTH:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 4);
                if (locationOFF) {  // 如果用户没有开启位置服务, 则引导跳转至设置界面
                    startActivityForResult(intentToLocationON, 10);
                }
                boolean bluetoothOn = true;
                try {
                    bluetoothOn = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.BLUETOOTH_ON) == 1);
                    Intent intentToBluetoothOn = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    if (!bluetoothOn) {  // 如果用户没有开启蓝牙, 则引导跳转至设置界面
                        startActivityForResult(intentToBluetoothOn, 11);
                    }
                } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
                break;
            case R.id.cbAUDIO:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.RECORD_AUDIO}, 5);
                break;
            case R.id.cbVIDEO:
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 6);
                if (!Settings.canDrawOverlays(this)) {  // 如果用户没有授权开启悬浮框, 则引导跳转至设置界面
                    Intent intentToFloatWindow = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    // intentToFloatWindow.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intentToFloatWindow, 7);  // 这里有一个回调
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int getAllPermissions = 0;
        for (int grantResult : grantResults) {
            getAllPermissions += grantResult;
        }
        if (getAllPermissions == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case 12:  // 这两步顺序一定不能倒过来
                    forcedStop = true;
                    tbStartStop.setChecked(false);
                    break;
                case 1:
                    cbWifi.setChecked(false);
                    break;
                case 2:
                    cbTele.setChecked(false);
                    break;
                case 3:
                    cbLocation.setChecked(false);
                    break;
                case 4:
                    cbBluetooth.setChecked(false);
                    break;
                case 5:
                    cbAudio.setChecked(false);
                    break;
                case 6:
                    cbVideo.setChecked(false);
                    break;
            }
        }
        else {
            if (requestCode == 12) {
                startEverything();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        boolean locationOFF_2 = true;

        // 这里可以检查用户是否授予了相应权限, 并做出反应
        switch (requestCode) {
            case 7:
                if (!Settings.canDrawOverlays(this)) {  // 如果用户依然不授权悬浮框权限, 则阻止用户选择该选项
                    cbVideo.setChecked(false);
                } break;
            case 8:
                try {
                    locationOFF_2 = (Settings.Secure.LOCATION_MODE_OFF == Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE));
                    if (locationOFF_2) {  // 如果用户依然不开启位置服务, 则阻止用户选择该选项
                        cbWifi.setChecked(false);
                    }
                } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
                 break;
            case 9:
                try {
                    locationOFF_2 = (Settings.Secure.LOCATION_MODE_OFF == Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE));
                    if (locationOFF_2) {  // 如果用户依然不开启位置服务, 则阻止用户选择该选项
                        cbLocation.setChecked(false);
                    }
                } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
                 break;
            case 10:
                try {
                    locationOFF_2 = (Settings.Secure.LOCATION_MODE_OFF == Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE));
                    if (locationOFF_2) {  // 如果用户依然不开启位置服务, 则阻止用户选择该选项
                        cbBluetooth.setChecked(false);
                    }
                } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
                break;
            case 11:
                boolean bluetoothOn_2 = true;
                try {
                    bluetoothOn_2 = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.BLUETOOTH_ON) == 1);
                    if (!bluetoothOn_2) {  // 如果用户依然不开启蓝牙, 则阻止用户选择该选项
                        cbBluetooth.setChecked(false);
                    }
                } catch (Settings.SettingNotFoundException e) { e.printStackTrace(); }
                break;
        }

    }
}
