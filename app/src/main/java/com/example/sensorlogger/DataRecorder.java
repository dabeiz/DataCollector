package com.example.sensorlogger;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DataRecorder {

    private String TAG = "DataRecorder";

    public DataRecorder() {}

    public void recordData(String filename, String data) {
        try {
            // 如果使用外部存储, 则创建本应用的文件夹 /storage/emulated/0/mylogger , 实际是在/sdcard/mylogger 里
            File fileDir = new File(Environment.getExternalStorageDirectory() + "/mylogger");

            // 如果使用内部存储, 则直接使用本应用的文件夹 /data/user/0/com.example.sensorlogger/files, 实际是在/data/data/com.example.sensorlogger/files, 但是使用内部存储时, 用手机直接找不到...
            // File fileDir = new File(getFilesDir().toString());
            if (!fileDir.exists()) {
                boolean mkdirChecked = fileDir.mkdirs();
                if (!mkdirChecked) {
                    Log.e(TAG, "Directory creation failed.");
                }
            }

            // 然后在本应用文件夹下创建文件, 初步是存为 .txt模式, 或许考虑存为专门的数据格式
            File fileData = new File(fileDir, filename + ".txt");

            // 开始往文件里写入数据
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileData, true);   // 与要写入的那个文件建立的输出流, 且模式设为"追加写入"
                fos.write(data.getBytes(StandardCharsets.UTF_8));  // 写入数据
                fos.close();  // 关闭该输出流
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
