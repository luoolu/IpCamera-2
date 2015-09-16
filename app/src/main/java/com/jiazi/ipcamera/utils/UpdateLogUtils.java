package com.jiazi.ipcamera.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;

/**
 * Created by Administrator on 2015/9/10.
 */
public class UpdateLogUtils {
    private String UPDATE_LOG = "";
    private int currentVersion;
    private Context mContext;

    public UpdateLogUtils(Context context) {
        mContext = context;
    }

    public void showUpdateLog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        builder.title("更新日志")
                .content(getUpdateLog())
                .positiveText("确定").positiveColor(mContext.getResources().getColor(R.color.colorPrimary))
                .show();
    }

    /**
     * 获取日志
     */
    private String getUpdateLog() {

        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            currentVersion = packageInfo.versionCode;          //取得APP的版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = currentVersion; i > 0; i--) {
            String versionName = getVersionName(i);
            if (!TextUtils.isEmpty(versionName)) {
                UPDATE_LOG = UPDATE_LOG + "\n" + versionName + "\n" + getLogContent(i);
            }
        }
        return UPDATE_LOG;
    }

    private String getVersionName(int version) {
        String versionName = null;
        switch (version) {
            case 180:
                versionName = "Version 1.8.0";
                break;
            case 181:
                versionName = "Version 1.8.1";
                break;
            case 182:
                versionName = "Version 1.8.2";
                break;
            case 183:
                versionName = "Version 1.8.3";
                break;
            default:
                break;
        }
        return versionName;
    }

    private String getLogContent(int version) {
        String updateLog = null;
        switch (version) {
            case 180:
                updateLog = "1.甲子中心为主页面，并且显示蓝牙设备信息\n" +
                        "2.增添检查更新功能，有新版本时提醒更新\n" +
                        "3.修复一些BUG\n";
                break;
            case 181:
                updateLog = "1.优化蓝牙设备的显示\n" +
                        "2.增添查看更新日志功能\n" +
                        "3.可以在应用里面查看更新\n";
                break;
            case 182:
                updateLog = "1.更新完显示更新日志\n" +
                        "2.增添意见反馈功能\n" +
                        "3.一些UI变化及BUG修复\n";
                break;
            case 183:
                updateLog = "1.在应用内部打开公司网站\n" +
                        "2.一些小更改\n";
                break;
            default:
                break;
        }
        return updateLog;
    }

}
