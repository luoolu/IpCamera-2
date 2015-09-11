package com.jiazi.ipcamera.asyncTask;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.customView.ScaleImageView;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 获取摄像头的监控范围并且存储到数据库
 */
public class SavePosAsyncTask extends AsyncTask<String, Void, Boolean> {

    private String website;
    private CameraManager mCameraManager;
    private Activity mActivity;
    private ScaleImageView mScaleImageView;
    private MaterialDialog loadDialog;
    private ListView mListView;
    private LayoutInflater mInflater;
    private CardView mCardView;

    public SavePosAsyncTask(Activity mActivity, CameraManager cameraManager, String website,
                            ScaleImageView mScaleImageView, MaterialDialog loadDialog,
                            ListView mListView, LayoutInflater mInflater, CardView mCardView) {
        this.mActivity = mActivity;
        mCameraManager = cameraManager;
        this.website = website;
        this.mScaleImageView = mScaleImageView;
        this.loadDialog = loadDialog;
        this.mListView = mListView;
        this.mInflater = mInflater;
        this.mCardView = mCardView;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String result = HttpUtil.getData(website);
        JSONObject object;
        if (result != null) {
            try {
                object = new JSONObject(result);
                /**
                 * 在你获取的string这个JSON对象中，提取你所需要的信息。
                 */
                String resultCode = object.getString("code");
                if (resultCode.equals("301")) {    //查找失败
                    Toast.makeText(mActivity, "监控位置查找失败", Toast.LENGTH_SHORT).show();
                } else if (resultCode.equals("300")) {                 //查找成功
                    JSONArray datas = object.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject data = (JSONObject) datas.get(i);
                        String uid = data.getString("uid");
                        String xstartPos = data.getString("x_start");
                        String xstopPos = data.getString("x_end");
                        String ystartPos = data.getString("y_start");
                        String ystopPos = data.getString("y_end");

                        /**
                         * 将信息存到数据库
                         */
                        CameraBean mCamera = mCameraManager.getCameraByUID(uid);
                        if (mCamera != null) {
                            mCamera.setxStart(Float.parseFloat(xstartPos));
                            mCamera.setxStop(Float.parseFloat(xstopPos));
                            mCamera.setyStart(Float.parseFloat(ystartPos));
                            mCamera.setyStop(Float.parseFloat(ystopPos));
                            mCameraManager.changeDevice(mCamera);
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;                   //查找监控范围并且存储到数据库失败
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            /**
             *  开启后台线程获取图片
             */
            GetMapAsyncTask getMapAsyncTask = new GetMapAsyncTask(mActivity, mScaleImageView,
                    loadDialog, mListView, mInflater, mCardView);     //后台线程获取图片
            getMapAsyncTask.execute();
        }
    }
}
