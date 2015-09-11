package com.jiazi.ipcamera.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.activity.DownloadActivity;
import com.jiazi.ipcamera.bean.AppBean;
import com.jiazi.ipcamera.utils.UpdateThread;

import java.text.DecimalFormat;

/**
 * 后台下载服务
 */
public class DownloadService extends Service {
    private int NOTICE_ID = 1;

    public static final String UPDATE_APK_START = "update_apk_start";
    public static final String UPDATE_APK_UPDATE = "update_apk_update";
    public static final String UPDATE_APK_FINISH = "update_apk_finish";
    public static final String UPDATE_APK_FAIL = "update_apk_fail";

    private Context context;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private UpdateThread updateThread;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_APK_START);
        filter.addAction(UPDATE_APK_UPDATE);
        filter.addAction(UPDATE_APK_FINISH);
        filter.addAction(UPDATE_APK_FAIL);
        registerReceiver(mReceiver, filter);// 代码注册Receiver
    }

    /**
     * 初始化notification
     */
    private void initNotification(String title, int progress, int max, int size) {
        if (notification == null) {

            Intent intent = new Intent(context, DownloadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //自定义通知布局
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.update_notification);

            notification = new Notification();
            notification.icon = R.drawable.ic_launcher;

            notification.contentIntent = pi;
            notification.contentView = rv;

            startForeground(NOTICE_ID, notification);      //让服务前台运行

        }
        notification.tickerText = title;

        notification.contentView.setTextViewText(R.id.tv_update_apk, title);
        notification.contentView.setTextViewText(R.id.tv_update_percent, "0%");
        notification.contentView.setProgressBar(R.id.pb_update_apk, max, progress, false);
        float size1 = (float) size / (1024 * 1024);
        DecimalFormat df = new DecimalFormat("0.00"); // 保留两位小数
        String sizeText = df.format(size1);
        notification.contentView.setTextViewText(R.id.tv_apk_size, sizeText + "MB");
        mNotificationManager.notify(NOTICE_ID, notification);
    }

    /**
     * 更新notification
     */
    private synchronized void updateNotification(int progress, int max) {
        notification.contentView.setTextViewText(R.id.tv_update_percent, progress * 100 / max + "%");
        notification.contentView.setProgressBar(R.id.pb_update_apk, max, progress, false);
        mNotificationManager.notify(NOTICE_ID, notification);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case UPDATE_APK_START:           //开始下载
                    AppBean appBean = (AppBean) intent.getSerializableExtra("info");
                    updateThread = new UpdateThread(context, appBean);
                    updateThread.start();
                    initNotification("正在下载：" + appBean.fileName, 0, 100, appBean.size);
                    break;
                case UPDATE_APK_UPDATE:         //更新下载进度
                    int progress = intent.getIntExtra("progress", 0);
                    int max = intent.getIntExtra("max", 100);
                    updateNotification(progress, max);
                    break;
                case UPDATE_APK_FINISH:         //下载完成
                    stopForeground(true);
                    mNotificationManager.cancel(NOTICE_ID);
                    updateThread.installAPK();
                    break;
                case UPDATE_APK_FAIL:
                    notification.contentView.setTextViewText(R.id.tv_update_apk, "下载失败");
                    notification.tickerText = "下载失败";
                    mNotificationManager.notify(NOTICE_ID, notification);
                    stopForeground(true);
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}