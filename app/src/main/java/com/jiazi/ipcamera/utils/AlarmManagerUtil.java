package com.jiazi.ipcamera.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * 定时查询报警的定时器工具类
 */
public class AlarmManagerUtil {

    private String broadcastCotent;

    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public AlarmManagerUtil(String broadcastCotent) {
        this.broadcastCotent = broadcastCotent;
    }

    /**
     * 指定时间后进行更新设备安全信息
     */
    public void sendUpdateBroadcast(Context context) {
        AlarmManager mAlarmManager = getAlarmManager(context);
        // 5秒后将产生广播,触发UpdateReceiver的执行
        Intent intent = new Intent(broadcastCotent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, pendingIntent);
    }

    /**
     * 取消定时执行
     */
    public void cancelUpdateBroadcast(Context context) {
        AlarmManager mAlarmManager = getAlarmManager(context);
        Intent intent = new Intent(broadcastCotent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mAlarmManager.cancel(pendingIntent);
    }
}
