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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用于添加摄像头数据到服务器的后台线程
 */
public class AddCameraAsyncTask extends AsyncTask<String, Void, Boolean> {

    private Activity mActivity;
    private String websiteHead;
    private CameraBean camera;
    private MaterialDialog mMaterialDialog;

    public AddCameraAsyncTask(Activity mActivity, String websiteHead, CameraBean camera, MaterialDialog mMaterialDialog) {
        this.mActivity = mActivity;
        this.websiteHead = websiteHead;
        this.camera = camera;
        this.mMaterialDialog = mMaterialDialog;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String uid = camera.getDid();
        String username = camera.getName();
        String password = camera.getPsw();
        String nickname = camera.getNickname();
        String website = websiteHead + "&uid=" + uid + "&user=" + username + "&pwd=" + password
                + "&remark=" + nickname;
        String result = HttpUtil.connect(website);
        if (result != null) {
            try {
                JSONObject object = new JSONObject(result);
                String resultCode = object.getString("code");
                if (resultCode.equals("308")) {           //添加摄像头成功
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
            CameraManager.getInstance(mActivity).addCamera(camera);         //增加摄像头信息到数据库
            Intent intent = new Intent(mActivity, CameraListActivity.class);
            mActivity.startActivity(intent);
            if (mMaterialDialog != null) {
                mMaterialDialog.dismiss();
            }
            mActivity.finish();
            Toast.makeText(mActivity, "添加摄像头成功", Toast.LENGTH_SHORT).show();
        } else {
            if (mMaterialDialog != null) {
                mMaterialDialog.dismiss();
            }
            Toast.makeText(mActivity, "添加摄像头失败，请再次尝试", Toast.LENGTH_SHORT).show();
        }
    }
}
