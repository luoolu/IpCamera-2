package com.jiazi.ipcamera.asyncTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.activity.AboutActivity;
import com.jiazi.ipcamera.activity.DownloadActivity;
import com.jiazi.ipcamera.bean.AppBean;
import com.jiazi.ipcamera.service.DownloadService;
import com.jiazi.ipcamera.utils.HttpUtil;
import com.jiazi.ipcamera.utils.MD5Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * 检查更新的线程
 */
public class UpdateAsyncTask extends AsyncTask<String, Void, String> {

    public static final String APP_ID = "2";// 数据库APP分配的ID
    public static final String API_TOKEN = "p5*d&z9-w8lq";// 加密字符

    public static final String URL_UPDATE_INDEX = "http://mobileapp.jiazi-it.com/index.php?m=Home&c=VersionApi";// APP管理的URL
    public static final String URL_UPDATE_APK = URL_UPDATE_INDEX + "&a=checkForUpdate";// 检查更新APP的URL
    private Activity mActivity;

    public UpdateAsyncTask(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            PackageManager packageManager = mActivity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mActivity.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;          //取得APP的版本号

            // 返回的类型跟onPostExecute中的参数必须一致
            String time = Long.toString(System.currentTimeMillis());
            String path = URL_UPDATE_APK + "&time=" + time + "&token="
                    + MD5Util.getMD5(time + API_TOKEN, 32) + "&app_id=" + APP_ID + "&version_code="
                    + versionCode;
            return HttpUtil.getData(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            try {
                JSONObject object = new JSONObject(result);
                int code = object.getInt("code");
                switch (code) {
                    case 104:              //有最新版本
                        JSONObject data = object.getJSONObject("data");
                        final AppBean appBean = new AppBean();
                        appBean.id = data.getInt("id");
                        appBean.versionCode = data.getInt("vercode");
                        appBean.versionName = data.getString("vername");
                        appBean.url = data.getString("url");
                        appBean.content = data.getString("content");
                        appBean.size = Integer.parseInt(data.getString("size"));
                        appBean.fileName = data.getString("savename");
                        float size = (float) appBean.size / (1024 * 1024);
                        DecimalFormat df = new DecimalFormat("0.00"); // 保留两位小数
                        String sizeText = df.format(size) + "MB";
                        if (mActivity != null) {
                            MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
                            builder.title("检查到新版本，是否立刻更新？")
                                    .content("新版本:" + appBean.versionName + "   " + sizeText + "\n" + appBean.content)
                                    .positiveText("更新").positiveColor(mActivity.getResources().getColor(R.color.colorPrimary))
                                    .negativeText("取消").negativeColor(mActivity.getResources().getColor(R.color.colorPrimary))
                                    .neutralText("不再提醒").neutralColor(mActivity.getResources().getColor(R.color.colorPrimary))
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            Intent intent = new Intent(DownloadService.UPDATE_APK_START);
                                            intent.putExtra("info", appBean);
                                            mActivity.sendBroadcast(intent);
                                            Intent intent1 = new Intent(mActivity, DownloadActivity.class);
                                            mActivity.startActivity(intent1);
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            super.onNegative(dialog);
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onNeutral(MaterialDialog dialog) {
                                            super.onNeutral(dialog);
                                            SharedPreferences mSharedPreferences = mActivity.getSharedPreferences("SharedPreferences", Activity.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                                            editor.putBoolean("auto_update", false);
                                            editor.commit();
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                        break;
                    case 103:
                        Toast.makeText(mActivity, "获取版本信息失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 105:                         //当前版本已为最新
                        if (mActivity instanceof AboutActivity) {
                            Toast.makeText(mActivity, "当前版本已为最新版本", Toast.LENGTH_SHORT).show();
                        }
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mActivity, "数据解析出错", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
