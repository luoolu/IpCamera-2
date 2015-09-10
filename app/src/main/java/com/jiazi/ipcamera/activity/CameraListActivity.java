package com.jiazi.ipcamera.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.adapter.StartVideoListAdapter;
import com.jiazi.ipcamera.asyncTask.DeviceAlarmAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.utils.AlarmManagerUtil;
import com.jiazi.ipcamera.utils.CameraManager;

import java.util.List;


/**
 * 展示所有摄像头的列表以及报警信息
 */
public class CameraListActivity extends AppCompatActivity {

    public static String AlarmUrl = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=openMonitor&token=96f86412b37fe9665c5ec4c3fad04f0f";
    public static final String BROADCAST_ACTION = "com.jiazi.ipcamera.alarm";

    private Context mContext;
    private RecyclerView mRecyclerView;
    private StartVideoListAdapter mAdapter;
    private List<CameraBean> cameras;
    private FloatingActionButton mFAB;
    private ImageButton backImageButton;
    private CoordinatorLayout mCoordinatorLayout;
    private UpdateReceiver mReceiver;
    private CameraManager mCameraManager;
    private AlarmManagerUtil mAlarmManagerUtil;
    public static boolean isTipFirstOpen;
    public static boolean isFirstAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_list);
        mContext = this;
        isTipFirstOpen = true;
        isFirstAlarm = true;
        mAlarmManagerUtil = new AlarmManagerUtil(BROADCAST_ACTION);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_start_fragment);

        mFAB = (FloatingActionButton) findViewById(R.id.add_action);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraListActivity.this, AddCameraActivity.class);
                startActivity(intent);
            }
        });

        backImageButton = (ImageButton) findViewById(R.id.btn_back);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));   // 设置LinearLayoutManager
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());    // 设置ItemAnimator
        mRecyclerView.setHasFixedSize(true);    // 设置固定大小

        mCameraManager = CameraManager.getInstance(this);
        mReceiver = new UpdateReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION);
        registerReceiver(mReceiver, filter);
        mCameraManager = CameraManager.getInstance(this);
        setAdapter();
        if (cameras.size() != 0) {
            mAlarmManagerUtil.sendUpdateBroadcast(this);
        }
        if (cameras.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            Snackbar.make(mCoordinatorLayout, getString(R.string.please_add_camera), Snackbar.LENGTH_SHORT).show();
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapter() {
        cameras = mCameraManager.getCameras();
        mAdapter = new StartVideoListAdapter(this, cameras, mCameraManager);     // 初始化自定义的适配器
        mRecyclerView.setAdapter(mAdapter);       // 为mRecyclerView设置适配器
    }


    @Override
    protected void onStop() {
        super.onStop();
        mAlarmManagerUtil.cancelUpdateBroadcast(this);
        unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    /**
     * 注：onReceive（）方法在10s内没有执行完毕，Android会认为该程序无响应
     */
    public class UpdateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Message msg = new Message();
            mHandler.sendMessage(msg);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceAlarmAsyncTask deviceAlarmAsyncTask = new DeviceAlarmAsyncTask(AlarmUrl, mContext, mCameraManager,
                    mCoordinatorLayout, mRecyclerView, isTipFirstOpen, isFirstAlarm, mAlarmManagerUtil);
            deviceAlarmAsyncTask.execute();
        }
    };


}
