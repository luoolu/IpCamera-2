package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.asyncTask.SavePosAsyncTask;
import com.jiazi.ipcamera.asyncTask.UpdateAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.customView.ScaleImageView;
import com.jiazi.ipcamera.fragment.CameraFragment;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.service.DownloadService;
import com.jiazi.ipcamera.utils.AlarmManagerUtil;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.UpdateLogUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 显示手环地图
 */
public class ShowMapActivity extends AppCompatActivity {

    public static final String mapWeb = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=getOrgPic&token=96f86412b37fe9665c5ec4c3fad04f0f";
    public static final String BROADCAST_MAP = "com.jiazi.ipcamera.map";

    private ScaleImageView mImageView;
    private CameraManager mCameraManager;
    private List<CameraBean> mCameraList;
    private Context mContext;
    private FloatingActionButton mFAB;
    private MaterialDialog loadDialog;
    private AlarmManagerUtil mAlarmManagerUtil;
    private UpdateReceiver mReceiver;
    private MaterialDialog progressDialog;
    private Toolbar mToolBar;
    private CardView listCardView;
    private ListView mDeviceLv;

    private int isAuto = 1;      //0为手动更新    1为自动更新
    public static boolean isShowing = false;         //没有警告框

    private SharedPreferences mSharedPreferences;
    public static boolean autoUpdate;
    public boolean showLogDialog;

    /**
     * 顶端、底端、左端、右端参数
     */
    public static ArrayList<Integer> topPos;
    public static ArrayList<Integer> bottomPos;
    public static ArrayList<Integer> leftPos;
    public static ArrayList<Integer> rightPos;

    public static boolean isPosSet = false;    //是否根据图片设置了宽高参数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        mContext = this;

        mAlarmManagerUtil = new AlarmManagerUtil(BROADCAST_MAP);

        mToolBar = (Toolbar) findViewById(R.id.toolbar_start);
        mToolBar.setTitle("甲子中心");
        setSupportActionBar(mToolBar);           //设置ToolBar为ActionBar

        listCardView = (CardView) findViewById(R.id.cardview_list);
        mDeviceLv = (ListView) findViewById(R.id.lv_deviceinfo);

        mImageView = (ScaleImageView) findViewById(R.id.div_main);
        mFAB = (FloatingActionButton) findViewById(R.id.fab_refresh_map);

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPicture();
            }
        });

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        progressDialog = builder
                .title("提示")
                .content("正在初始化...")
                .progress(true, 0)
                .cancelable(false)
                .progressIndeterminateStyle(false)
                .show();

        startService();         //开启桥接服务
        startService(new Intent(this, DownloadService.class)); //开启后台下载服务

        mReceiver = new UpdateReceiver();

        mSharedPreferences = getSharedPreferences("SharedPreferences", Activity.MODE_PRIVATE);
        showLogDialog = mSharedPreferences.getBoolean("log_dialog", true);
        if (showLogDialog) {
            UpdateLogUtils updateLogUtils = new UpdateLogUtils(this);
            updateLogUtils.showUpdateLog();
            showLogDialog = false;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("log_dialog", false);
            editor.commit();
        }
        autoUpdate = mSharedPreferences.getBoolean("auto_update", true);
        if (autoUpdate) {               //检查更新
            UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this);
            updateAsyncTask.execute();
        }

    }

    /**
     * 开启桥接服务
     */
    public void startService() {
        Intent intent = new Intent();
        intent.setClass(ShowMapActivity.this, BridgeService.class);
        startService(intent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vstc2.nativecaller.NativeCaller.PPPPInitialOther("ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL");
                    Thread.sleep(1000);
                    Message msg = new Message();
                    mHandler.sendMessage(msg);
                } catch (Exception e) {

                }
            }
        }).start();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            Toast.makeText(ShowMapActivity.this, R.string.toast_service_success, Toast.LENGTH_SHORT).show();
            loadPicture();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        topPos = new ArrayList<Integer>();
        bottomPos = new ArrayList<Integer>();
        leftPos = new ArrayList<Integer>();
        rightPos = new ArrayList<Integer>();
        mCameraManager = CameraManager.getInstance(this);
        loadPicture();                //接收到广播后加载手环地图
        initReceiver();
        mCameraList = mCameraManager.getCameras();
        if (isAuto == 1) {
            mAlarmManagerUtil.sendUpdateBroadcast(this);
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_MAP);
        registerReceiver(mReceiver, filter);
    }

    private void loadPicture() {
        if (isAuto == 0) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            loadDialog = builder
                    .title("提示")
                    .content("正在加载，请稍候...")
                    .cancelable(true)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .show();
        }
        /**
         * 开启线程获取摄像头的监控范围并且存储到数据库
         */
        SavePosAsyncTask savePosAsyncTask = new SavePosAsyncTask(this, mCameraManager,
                CameraFragment.WEBSITE, mImageView, loadDialog, mDeviceLv, getLayoutInflater(), listCardView);
        savePosAsyncTask.execute();

    }

    /**
     * 显示更新方式的对话框
     */
    private void showChooseDialog() {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("更新手环地图方式")
                .items(new String[]{"手动更新", "自动更新"})
                .itemsCallbackSingleChoice(isAuto, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        if (i == 0) {
                            mFAB.setVisibility(View.VISIBLE);
                            Toast.makeText(ShowMapActivity.this, charSequence, Toast.LENGTH_SHORT).show();
                            mAlarmManagerUtil.cancelUpdateBroadcast(ShowMapActivity.this);      //取消发送广播
                            unregisterReceiver(mReceiver);                                    //取消注册接收器
                            isAuto = 0;
                        } else if (i == 1) {
                            initReceiver();
                            mAlarmManagerUtil.sendUpdateBroadcast(ShowMapActivity.this);
                            Toast.makeText(ShowMapActivity.this, charSequence, Toast.LENGTH_SHORT).show();
                            mFAB.setVisibility(View.GONE);
                            isAuto = 1;
                        }
                        return true;
                    }
                })
                .positiveText("确认").positiveColor(getResources().getColor(R.color.colorPrimary))
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showQuitDialog();
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();        //取得触摸位置的绝对x坐标

        float y = event.getRawY();        //取得触摸位置的绝对y坐标
        Log.i("tag", x + "  " + y);
        switch (event.getAction()) {
            //触摸屏幕时刻
            case MotionEvent.ACTION_DOWN:
                if (isPosSet) {
                    for (int i = 0; i < mCameraList.size(); i++) {
                        if ((x < rightPos.get(i)) && (x > leftPos.get(i))
                                && (y > topPos.get(i)) && (y < bottomPos.get(i))) {
                            CameraBean camera = mCameraList.get(i);
                            showConfirmDialog(camera);
                        }
                    }
                }
                break;
            //触摸并移动时刻
            case MotionEvent.ACTION_MOVE:

                break;
            //终止触摸时刻
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    public boolean showConfirmDialog(final CameraBean camera) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("提示")
                .content("跳转到摄像头: " + camera.getNickname())
                .positiveText("确定").positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText("取消").negativeColor(getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent intent = new Intent(mContext, CameraActivity.class);
                        String uid = camera.getDid();
                        String username = camera.getName();
                        String password = camera.getPsw();
                        String devName = camera.getNickname();
                        intent.putExtra("uid", uid);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        intent.putExtra("devName", devName);
                        CameraFragment.fromMapActivity = true;
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
        return false;
    }

    /**
     * 重复加载手环地图
     */
    public class UpdateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            loadPicture();                //接收到广播后加载手环地图
            mAlarmManagerUtil.sendUpdateBroadcast(ShowMapActivity.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_show_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.camera_list) {
            Intent intent = new Intent(ShowMapActivity.this, CameraListActivity.class);
            startActivity(intent);
        } else if (i == R.id.refresh_map) {
            showChooseDialog();
        } else if (i == R.id.about_us) {
            Intent intent = new Intent(ShowMapActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isAuto == 1) {
            mAlarmManagerUtil.cancelUpdateBroadcast(this);
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vstc2.nativecaller.NativeCaller.Free();
        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        stopService(intent);
    }

    /****
     * 退出确定dialog
     */
    public void showQuitDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(getResources().getString(R.string.exit) + "智能监护")
                .content(R.string.exit_alert)
                .positiveText(R.string.str_ok).positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText(R.string.str_cancel).negativeColor(getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        ShowMapActivity.this.finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }
}
