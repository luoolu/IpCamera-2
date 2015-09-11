package com.jiazi.ipcamera.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改摄像头用户名、密码的后台线程
 */
public class ChangeCameraAsyncTask extends AsyncTask<String, Void, Boolean> {

    private Context mContext;
    private String webHead;
    private String uid;
    private String username;
    private String password;

    public ChangeCameraAsyncTask(Context context, String webHead, String uid, String username, String password) {
        mContext = context;
        this.webHead = webHead;
        this.uid = uid;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String website = webHead + "&uid=" + uid + "&user=" + username + "&pwd=" + password;
        String result = HttpUtil.getData(website);
        if (result != null) {
            try {
                JSONObject object = new JSONObject(result);
                String resultCode = object.getString("code");
                if (resultCode.equals("308")) {           //修改摄像头成功
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            Toast.makeText(mContext,"修改后的摄像头数据已上传到服务器",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext,"修改后的摄像头数据上传失败",Toast.LENGTH_SHORT).show();
        }
    }

}
