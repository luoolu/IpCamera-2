package com.jiazi.ipcamera.asyncTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jiazi.ipcamera.activity.CameraListActivity;
import com.jiazi.ipcamera.adapter.StartVideoListAdapter;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.utils.AlarmManagerUtil;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 根据返回的网站的数据获取报警信息的后台线程
 */
public class DeviceAlarmAsyncTask extends AsyncTask<String, Void, Integer> {

    private String website;
    private Context mContext;
    private CameraManager mCameraManager;
    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mRecyclerView;
    private AlarmManagerUtil mAlarmManagerUtil;
    private boolean isTipFirstOpen;
    private boolean isFirstAlarm;

    /**
     * 构造函数
     */
    public DeviceAlarmAsyncTask(String website, Context mContext, CameraManager mCameraManager,
                                CoordinatorLayout mCoordinatorLayout, RecyclerView mRecyclerView,
                                boolean isTipFirstOpen, boolean isFirstAlarm, AlarmManagerUtil mAlarmManagerUtil) {
        this.website = website;
        this.mContext = mContext;
        this.mCameraManager = mCameraManager;
        this.mCoordinatorLayout = mCoordinatorLayout;
        this.mRecyclerView = mRecyclerView;
        this.isTipFirstOpen = isTipFirstOpen;
        this.isFirstAlarm = isFirstAlarm;
        this.mAlarmManagerUtil = mAlarmManagerUtil;
    }

    /**
     * 后台线程处理
     */
    @Override
    protected Integer doInBackground(String... strings) {
        String result = HttpUtil.getData(website);
        JSONObject object;
        String uid;
        String mac;
        if (result != null) {
            try {
                object = new JSONObject(result);
                /**
                 * 在你获取的string这个JSON对象中，提取你所需要的信息。
                 */
                String resultCode = object.getString("code");
                if (resultCode.equals("302")) {                 //报警设备存在
                    JSONArray datas = object.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject data = (JSONObject) datas.get(i);
                        uid = data.getString("uid");
                        mac = data.getString("mac");
                        initAlarmInfo();
                        if (uid != null) {
                            CameraBean camera = mCameraManager.getCameraByUID(uid);
                            if (camera != null) {
                                camera.setAlarminfo("Alarm");
                                camera.setMac(mac);
                                mCameraManager.changeDevice(camera);         //更新数据库的信息
                                return 0;
                            } else {
                                return 1;              //数据库中未收录无报警的摄像头的信息
                            }
                        }
                    }
                } else if (resultCode.equals("303")) {          //无报警摄像头
                    initAlarmInfo();
                    return 2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return 3;
            }
        }
        return 3;                      //从服务器获取数据失败
    }

    /**
     * 初始化所有摄像头的报警信息
     */
    private void initAlarmInfo() {
        List<CameraBean> cameras = mCameraManager.getCameras();
        for (int j = 0; j < cameras.size(); j++) {
            CameraBean cameraBean = cameras.get(j);
            cameraBean.setAlarminfo(null);
            mCameraManager.changeDevice(cameraBean);
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        switch (result) {
            case 0:
                /**
                 * 如果程序第一次创建，则显示提示信息
                 */
                if (isTipFirstOpen) {
                    Snackbar.make(mCoordinatorLayout, "长按摄像头取消报警", Snackbar.LENGTH_LONG).setAction("不再提示", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CameraListActivity.isTipFirstOpen = false;
                        }
                    }).show();
                    playAlertSound();
                }
                break;
            case 3:
                /**
                 * 用户可以取消网络异常的提示
                 */
                if (isFirstAlarm) {
                    Snackbar.make(mCoordinatorLayout, "网络异常，请检查网络情况", Snackbar.LENGTH_SHORT).setAction("不再提示", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CameraListActivity.isFirstAlarm = false;
                        }
                    }).show();
                }
        }
        List<CameraBean> cameras = mCameraManager.getCameras();
        StartVideoListAdapter adapter = new StartVideoListAdapter(mContext, cameras, mCameraManager);
        mRecyclerView.setAdapter(adapter);
        mAlarmManagerUtil.sendUpdateBroadcast(mContext);
    }


    private void playAlertSound() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(mContext).setSound(ringUri).build();
        notificationManager.notify(1, notification);
    }

}
