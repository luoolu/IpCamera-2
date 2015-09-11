package com.jiazi.ipcamera.asyncTask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.utils.HttpUtil;
import com.jiazi.ipcamera.utils.MD5Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用于上传反馈信息到服务器的线程
 */
public class FeedbackAsyncTask extends AsyncTask<String, Void, String> {

    public static final String APP_ID = "2";// 数据库APP分配的ID
    public static final String API_TOKEN = "p5*d&z9-w8lq";// 加密字符

    public static final String URL_UPDATE_INDEX = "http://mobileapp.jiazi-it.com/index.php?m=Home&c=VersionApi";// APP管理的URL
    public static final String URL_UPDATE_APK = URL_UPDATE_INDEX + "&a=addFeedback";// 问题反馈的URL

    private Context mContext;
    private String content;
    private MaterialDialog mDialog;

    public FeedbackAsyncTask(Context context, String content, MaterialDialog materialDialog) {
        mContext = context;
        this.content = content;
        mDialog = materialDialog;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            String versionName = packageInfo.versionName;          //取得APP的版本名称

            // 返回的类型跟onPostExecute中的参数必须一致
            String time = Long.toString(System.currentTimeMillis());
            //post数据前面不用加&
            String data = "time=" + time + "&token="
                    + MD5Util.getMD5(time + API_TOKEN, 32) + "&app_id=" + APP_ID + "&version_name="
                    + versionName + "&content=" + content;
            return HttpUtil.postData(URL_UPDATE_APK, data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i("FeedbackAsyncTask", result);
        if (result != null) {
            try {
                JSONObject object = new JSONObject(result);
                int code = object.getInt("code");
                switch (code) {
                    case 106:                        //添加成功
                        Toast.makeText(mContext, "反馈成功，谢谢你的支持", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        break;
                    default:
                        Toast.makeText(mContext, "反馈失败，请再次反馈你的意见", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "网络异常，请检查网络情况", Toast.LENGTH_SHORT).show();
        }
    }
}
