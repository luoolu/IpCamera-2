package com.jiazi.ipcamera.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.jiazi.ipcamera.bean.AppBean;
import com.jiazi.ipcamera.service.DownloadService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载APK线程
 */
public class UpdateThread extends Thread {
    private Context context;
    private AppBean mAppBean;
    private String filename;

    public UpdateThread(Context context, AppBean mAppBean) {
        this.context = context;
        this.mAppBean = mAppBean;
    }

    @Override
    public void run() {
        String path = Environment.getExternalStorageDirectory().getPath();
        filename = path + "/ipcamera/download";
        File dir = new File(filename);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 构建文件对象
        File file = new File(dir, mAppBean.fileName);
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream is = null;
        Intent intent = null;
        try {
            raf = new RandomAccessFile(file, "rwd");
            raf.setLength(mAppBean.size);

            connection = (HttpURLConnection) new URL(mAppBean.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);

            if (200 == connection.getResponseCode()) {
                is = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int len;
                int finished = 0;
                long time = System.currentTimeMillis();
                while ((len = is.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    // 整个文件的进度
                    finished += len;
                    // 线程的下载进度

                    // 隔一段时间发送广播更新ProgressBar
                    if (System.currentTimeMillis() - time > 250) {
                        time = System.currentTimeMillis();

                        intent = new Intent(DownloadService.UPDATE_APK_UPDATE);
                        intent.putExtra("name", mAppBean.fileName);
                        intent.putExtra("progress", finished);
                        intent.putExtra("max", mAppBean.size);
                        context.sendBroadcast(intent);
                    }
                }

                if (finished == mAppBean.size) {           //完成下载
                    intent = new Intent(DownloadService.UPDATE_APK_FINISH);
                    intent.putExtra("progress", finished);
                    intent.putExtra("max", mAppBean.size);
                    context.sendBroadcast(intent);
                }
            }

            // 每一条线程下载完成之后检查是否都执行完毕
        } catch (Exception e) {
            e.printStackTrace();
            intent = new Intent(DownloadService.UPDATE_APK_FAIL);
            context.sendBroadcast(intent);
        } finally {
            connection.disconnect();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 安装apk文件
     */
    public void installAPK() {
        File apkfile = new File(filename, mAppBean.fileName);
        if (!apkfile.exists()) {        //如果apk文件不存在
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}