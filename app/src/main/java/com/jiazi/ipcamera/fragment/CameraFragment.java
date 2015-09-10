package com.jiazi.ipcamera.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.activity.AddCameraActivity;
import com.jiazi.ipcamera.activity.PlayActivity;
import com.jiazi.ipcamera.asyncTask.DevicePosAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.SystemValue;

import vstc2.nativecaller.NativeCaller;

/**
 * 摄像头的具体信息界面
 */
public class CameraFragment extends Fragment implements BridgeService.IpcamClientInterface {

    private int option = ContentCommon.INVALID_OPTION;
    private static final String STR_DID = "did";
    private static final String STR_MSG_PARAM = "msgparam";

    public static final String WEBSITE = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=getAllCoord&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private TextView uidText;
    private EditText nameText;
    private ImageView video_pic;
    private String uid;
    private String username;
    private String password;
    private String devName;
    private ImageButton changeImageBtn;
    private ImageButton confirmImageBtn;
    private ImageButton deleteBtn;
    private FloatingActionButton mPlayCameraFAB;
    private CameraManager mCameraManager;
    private TextView devInfo;
    private TextView tvPosX;
    private TextView tvPosY;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap;
    private int tag = 0;
    public static boolean fromMapActivity;
    public boolean returnMapActivity = false;

    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, container, false);
        uidText = (TextView) v.findViewById(R.id.frag_tv_dev_uid);
        nameText = (EditText) v.findViewById(R.id.frag_tv_dev_name);
        video_pic = (ImageView) v.findViewById(R.id.frag_image_camera);
        changeImageBtn = (ImageButton) v.findViewById(R.id.ib_changename);
        confirmImageBtn = (ImageButton) v.findViewById(R.id.ib_comfirmname);
        deleteBtn = (ImageButton) v.findViewById(R.id.btn_delete_camera);
        mPlayCameraFAB = (FloatingActionButton) v.findViewById(R.id.fab_play_camera);
        mPlayCameraFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, PlayActivity.class);
                startActivity(intent);
            }
        });
        devInfo = (TextView) v.findViewById(R.id.login_textView);
        tvPosX = (TextView) v.findViewById(R.id.tv_dev_xpos);
        tvPosY = (TextView) v.findViewById(R.id.tv_dev_ypos);
        DevicePosAsyncTask devicePosAsyncTask = new DevicePosAsyncTask(mActivity, uid, WEBSITE, tvPosX, tvPosY);
        devicePosAsyncTask.execute();

        mProgressBar = (ProgressBar) v.findViewById(R.id.login_progressBar);

        uidText.setText(uid);
        nameText.setText(devName);

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameText.setCursorVisible(true);                         //输入栏光标显示
                nameText.setSelection(nameText.getText().toString().length());       //光标移至文字末尾
                changeImageBtn.setVisibility(View.GONE);
                confirmImageBtn.setVisibility(View.VISIBLE);
            }
        });

        confirmImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = nameText.getText().toString();
                CameraManager mCameraManager = CameraManager.getInstance(mActivity);
                CameraBean camera = mCameraManager.getCameraByUID(uid);
                camera.setNickname(nickname);
                mCameraManager.changeDevice(camera);
                nameText.setCursorVisible(false);
                confirmImageBtn.setVisibility(View.GONE);
                changeImageBtn.setVisibility(View.VISIBLE);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();

            }
        });

        video_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, PlayActivity.class);
                startActivity(intent);
            }
        });
        done();
        return v;
    }

    private void showDeleteDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title("提示")
                .content("确定删除摄像头？")
                .positiveText("确定").positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText("取消").negativeColor(getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        new Thread(new deleteCameraThread()).start();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    class deleteCameraThread implements Runnable {
        @Override
        public void run() {
            mCameraManager.deleteCameraByUID(uid);
            try {
                Thread.sleep(500);
                mActivity.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mCameraManager = CameraManager.getInstance(mActivity);
        Intent intent = mActivity.getIntent();
        uid = intent.getStringExtra("uid");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        devName = intent.getStringExtra("devName");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (returnMapActivity) {
            getActivity().finish();
        }
    }

    private void done() {
        if (option == ContentCommon.INVALID_OPTION) {
            option = ContentCommon.ADD_CAMERA;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        SystemValue.deviceName = username;
        SystemValue.deviceId = uid;
        SystemValue.devicePass = password;
        BridgeService.setIpcamClientInterface(this);
        NativeCaller.Init();            //初始化视频解码器
        new Thread(new StartPPPPThread()).start();
    }

    //开启P2P连接
    class StartPPPPThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                startCameraPPPP();
                Log.i("CameraFragment","startCameraPPPP");
            } catch (Exception e) {

            }
        }
    }

    /**
     * 发送指令给摄像头，开启P2P连接
     */
    private void startCameraPPPP() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        int result = NativeCaller.StartPPPP(SystemValue.deviceId, SystemValue.deviceName,
                SystemValue.devicePass, 1, "");

    }


    @Override
    public void BSMsgNotifyData(String did, int type, int param) {
        Bundle bd = new Bundle();
        Message msg = PPPPMsgHandler.obtainMessage();
        msg.what = type;
        bd.putInt(STR_MSG_PARAM, param);
        bd.putString(STR_DID, did);
        msg.setData(bd);
        PPPPMsgHandler.sendMessage(msg);
    }

    @Override
    public void BSSnapshotNotify(String did, byte[] bImage, int len) {
        mBitmap = Bytes2Bimap(bImage, len);
        Message msg = SnapshotHandler.obtainMessage();
        SnapshotHandler.sendMessage(msg);

    }

    @Override
    public void callBackUserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {

    }

    @Override
    public void CameraStatus(String did, int status) {

    }

    private Handler PPPPMsgHandler = new Handler() {
        public void handleMessage(Message msg) {

            Bundle bd = msg.getData();
            int msgParam = bd.getInt(STR_MSG_PARAM);
            int msgType = msg.what;
            Log.i("Tag", msgParam + " ");
            String did = bd.getString(STR_DID);
            switch (msgType) {
                case ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS:
                    int resid;
                    switch (msgParam) {
                        case ContentCommon.PPPP_STATUS_CONNECTING://0
                            resid = R.string.pppp_status_connecting;
                            mProgressBar.setVisibility(View.VISIBLE);
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED://3
                            resid = R.string.pppp_status_connect_failed;
                            mProgressBar.setVisibility(View.GONE);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT://4
                            resid = R.string.pppp_status_disconnect;
                            mProgressBar.setVisibility(View.GONE);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING://1
                            resid = R.string.pppp_status_initialing;
                            mProgressBar.setVisibility(View.VISIBLE);
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_ID://5
                            resid = R.string.pppp_status_invalid_id;
                            mProgressBar.setVisibility(View.GONE);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_ON_LINE://2 在线状态
                            resid = R.string.pppp_status_online;
                            mProgressBar.setVisibility(View.GONE);
                            //摄像机在线之后读取摄像机类型
                            String cmd = "get_status.cgi?loginuse=admin&loginpas=" + SystemValue.devicePass
                                    + "&user=admin&pwd=" + SystemValue.devicePass;
                            NativeCaller.TransferMessage(did, cmd, 1);
                            tag = 1;
                            break;
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE://6
                            resid = R.string.device_not_on_line;
                            mProgressBar.setVisibility(View.GONE);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT://7
                            resid = R.string.pppp_status_connect_timeout;
                            mProgressBar.setVisibility(View.GONE);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER://8
                            resid = R.string.pppp_status_pwd_error;
                            mProgressBar.setVisibility(View.GONE);
                            showAlert();
                            tag = 0;
                            break;
                        default:
                            resid = R.string.pppp_status_unknown;
                            break;
                    }
                    if (mActivity != null && isAdded()) {
                        devInfo.setText(getResources().getString(resid));
                    }
                    if (tag == 1) {
                        getSnapshot();
                        if (fromMapActivity && mActivity != null && isAdded()) {
                            Intent intent = new Intent(mActivity, PlayActivity.class);
                            startActivity(intent);
                            returnMapActivity = true;
                        }
                    }
                    if (msgParam == ContentCommon.PPPP_STATUS_ON_LINE) {
                        NativeCaller.PPPPGetSystemParams(did,
                                ContentCommon.MSG_TYPE_GET_PARAMS);
                    }
                    if (msgParam == ContentCommon.PPPP_STATUS_INVALID_ID
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_FAILED
                            || msgParam == ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_ERRER) {
                        NativeCaller.StopPPPP(did);
                    }
                    break;
                case ContentCommon.PPPP_MSG_TYPE_PPPP_MODE:
                    break;
            }

        }
    };

    /**
     * 当密码错误时提示重新从服务器获取密码
     */
    private void showAlert() {
        mCameraManager.deleteCameraByUID(uid);
        Toast.makeText(mActivity, "密码错误，请重新添加摄像头", Toast.LENGTH_SHORT).show();
        if (mActivity != null && isAdded()) {
            Intent intent = new Intent(mActivity, AddCameraActivity.class);
            startActivity(intent);
            mActivity.finish();
        }
    }

    private Bitmap Bytes2Bimap(byte[] bytes, int len) {
        if (len != 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, len);
        } else {
            return null;
        }
    }

    /**
     * 摄像机在线时可以获取一张摄像机当前的画面图
     */
    private void getSnapshot() {
        String msg = "snapshot.cgi?loginuse=admin&loginpas=" + SystemValue.devicePass
                + "&user=admin&pwd=" + SystemValue.devicePass;
        NativeCaller.TransferMessage(SystemValue.deviceId, msg, 1);  //透传cgi指令
    }

    private Handler SnapshotHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            video_pic.setImageBitmap(mBitmap);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        NativeCaller.StopPPPP(uid);
    }

}
