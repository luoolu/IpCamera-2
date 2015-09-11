package com.jiazi.ipcamera.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用于取消报警信息的线程
 */
public class CancelAlertAsyncTask extends AsyncTask<String, Void, Boolean> {

    private static String webHead = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=closeAlarm&mac=";
    private static String webEnd = "&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private String mac;

    private CameraBean mCamera;

    private CameraManager mCameraManager;

    private ImageView devsafeIv;

    private Context mContext;

    /**
     * 构造函数
     */
    public CancelAlertAsyncTask(Context mContext, String mac, CameraBean mCamera, CameraManager mCameraManager, ImageView devsafeIv) {
        this.mContext = mContext;
        this.mac = mac;
        this.mCamera = mCamera;
        this.mCameraManager = mCameraManager;
        this.devsafeIv = devsafeIv;
    }

    /**
     * 后台线程操作
     */
    @Override
    protected Boolean doInBackground(String... strings) {
        String website = webHead + mac + webEnd;
        String result = HttpUtil.getData(website);
        JSONObject object;
        if (result != null) {
            try {
                object = new JSONObject(result);
                /**
                 * 在你获取的string这个JSON对象中，提取你所需要的信息。
                 */
                String resultCode = object.getString("code");
                if (resultCode.equals("304")) {                 //关闭报警成功
                    mCamera.setAlarminfo(null);
                    mCameraManager.changeDevice(mCamera);         //更新数据库的信息
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 前台展示Toast信息
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            devsafeIv.setImageResource(R.drawable.ic_safe);
        } else {
            Toast.makeText(mContext, "关闭报警失败", Toast.LENGTH_SHORT).show();
        }
    }
}
