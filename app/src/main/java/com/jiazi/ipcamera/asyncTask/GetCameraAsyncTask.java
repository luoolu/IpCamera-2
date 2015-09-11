package com.jiazi.ipcamera.asyncTask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.activity.CameraListActivity;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 获取摄像头信息的线程
 */
public class GetCameraAsyncTask extends AsyncTask<String, Void, Boolean> {

    private Activity mActivity;
    private String website;
    private CameraManager mCameraManager;
    private MaterialDialog mLoadDialog;

    public GetCameraAsyncTask(Activity mActivity, String website, MaterialDialog mLoadDialog) {
        this.mActivity = mActivity;
        this.website = website;
        this.mLoadDialog = mLoadDialog;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String result = HttpUtil.getData(website);
        JSONObject object;
        if (result != null) {
            try {
                object = new JSONObject(result);
                String resultCode = object.getString("code");
                if (resultCode.equals("310")) {           //获取摄像头信息成功
                    JSONArray datas = object.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject data = (JSONObject) datas.get(i);
                        String uid = data.getString("uid");
                        String username = data.getString("username");
                        String password = data.getString("password");
                        String remark = data.getString("remark");
                        Float xStart = Float.valueOf(data.getString("x_start"));
                        Float xStop = Float.valueOf(data.getString("x_end"));
                        Float yStart = Float.valueOf(data.getString("y_start"));
                        Float yStop = Float.valueOf(data.getString("y_end"));
                        mCameraManager = CameraManager.getInstance(mActivity);
                        if (mCameraManager.isExist(uid)) {        //本地数据库已经存在摄像头
                            CameraBean camera = mCameraManager.getCameraByUID(uid);
                            camera.setName(username);
                            camera.setPsw(password);
                            camera.setNickname(remark);
                            mCameraManager.changeDevice(camera);
                        } else {
                            CameraBean camera = new CameraBean(uid, username, password, remark, null, null, xStart, xStop, yStart, yStop);
                            mCameraManager.addCamera(camera);
                        }
                    }
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
            mLoadDialog.dismiss();
            Intent intent = new Intent(mActivity, CameraListActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
            Toast.makeText(mActivity, "更新摄像头数据成功", Toast.LENGTH_SHORT).show();
        } else {
            mLoadDialog.dismiss();
            Toast.makeText(mActivity, "更新摄像头数据失败", Toast.LENGTH_SHORT).show();
        }
    }
}
