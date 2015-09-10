package com.jiazi.ipcamera.utils;

import android.content.Context;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;

/**
 * Created by Administrator on 2015/9/10.
 */
public class UpdateLogUtils {
    private String UPDATE_LOG = "";
    private int currentVersion = 180;
    private MaterialDialog mMaterialDialog;
    private Context mContext;

    public UpdateLogUtils(Context context) {
        mContext = context;
    }

    public void showUpdateLog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        mMaterialDialog = builder
                .title("更新日志")
                .content(getUpdateLog())
                .positiveText("确定").positiveColor(mContext.getResources().getColor(R.color.colorPrimary))
                .show();
    }

    /**
     * 获取日志
     */
    private String getUpdateLog() {
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
                        "3.修复一些BUG";
                break;
            default:
                break;
        }
        return updateLog;
    }

}
