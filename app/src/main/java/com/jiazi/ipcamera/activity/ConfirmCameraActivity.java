package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.jiazi.ipcamera.asyncTask.AddCameraAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.SystemValue;

import vstc2.nativecaller.NativeCaller;

public class ConfirmCameraActivity extends AppCompatActivity implements BridgeService.IpcamClientInterface {

    private static final String WEB_HEAD = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=updateMinfo&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private static final String STR_DID = "did";
    private static final String STR_MSG_PARAM = "msgparam";
    private int option = ContentCommon.INVALID_OPTION;

    private ImageButton mBtnCancel;
    private ImageView mIvShowAndroid;
    private TextView mTvCameraUid;
    private TextInputLayout mTextInputUsername;
    private TextInputLayout mTextInputPassword;
    private TextInputLayout mTextInputDevname;
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;
    private EditText mEditTextDevname;
    private MaterialAnimatedSwitch mSwitchVisibility;
    private ImageButton mAdd;
    private TextView mTvCameraInfo;

    private MaterialDialog mMaterialDialog;
    private String uid;
    private CameraBean mCamera;
    private Context mContext;
    private Activity mActivity;
    private boolean pswShowed = false;
    private int tag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_camera);
        assignViews();
        mContext = this;
        mActivity = this;
        if (getIntent() != null) {
            uid = getIntent().getStringExtra("uid");
            mTvCameraUid.setText(uid);
        }
        BridgeService.setIpcamClientInterface(this);
    }

    /**
     * 控件初始化
     */
    private void assignViews() {

        mBtnCancel = (ImageButton) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmCameraActivity.this.finish();
            }
        });

        mAdd = (ImageButton) findViewById(R.id.add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                hideKeyboard(view);
                if (option == ContentCommon.INVALID_OPTION) {
                    option = ContentCommon.ADD_CAMERA;
                }
                if (checkValidate()) {
                    SystemValue.deviceName = mEditTextUsername.getText().toString();
                    SystemValue.deviceId = uid;
                    SystemValue.devicePass = mEditTextPassword.getText().toString();
                    NativeCaller.Init();            //初始化
                    new Thread(new StartPPPPThread()).start();
                    mTvCameraInfo.setVisibility(View.GONE);
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
                    mMaterialDialog = builder.cancelable(false)
                            .content("请稍候...")
                            .progress(true, 0)
                            .progressIndeterminateStyle(false)
                            .show();
                }
            }
        });

        mIvShowAndroid = (ImageView) findViewById(R.id.iv_show_android);
        mTvCameraUid = (TextView) findViewById(R.id.tv_camera_uid);
        mTextInputUsername = (TextInputLayout) findViewById(R.id.textInput_username);
        mEditTextUsername = mTextInputUsername.getEditText();

        mTextInputPassword = (TextInputLayout) findViewById(R.id.textInput_password);
        mEditTextPassword = mTextInputPassword.getEditText();
        mEditTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mIvShowAndroid.setImageResource(R.drawable.android_no_look);
                    mTextInputPassword.setErrorEnabled(false);
                } else {
                    mIvShowAndroid.setImageResource(R.drawable.android_look);
                }
            }
        });

        mTextInputDevname = (TextInputLayout) findViewById(R.id.textInput_devname);
        mEditTextDevname = mTextInputDevname.getEditText();
        mTvCameraInfo = (TextView) findViewById(R.id.tv_camera_info);

        /**
         * 处理按钮的点击事件
         */
        mSwitchVisibility = (MaterialAnimatedSwitch) findViewById(R.id.switch_lock);
        mSwitchVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pswShowed) {
                    mEditTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pswShowed = true;
                } else {
                    mEditTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pswShowed = false;
                }
            }
        });
    }

    /**
     * 检查输入的用户名密码设备备注是否为空
     */
    private boolean checkValidate() {
        if (TextUtils.isEmpty(mEditTextUsername.getText().toString())) {
            mTextInputUsername.setError("用户名不能为空");
            return false;
        } else {
            mTextInputUsername.setErrorEnabled(false);
        }
        if (TextUtils.isEmpty(mEditTextPassword.getText().toString())) {
            mTextInputPassword.setError("密码不能为空");
            return false;
        } else {
            mTextInputPassword.setErrorEnabled(false);
        }
        if (TextUtils.isEmpty(mEditTextDevname.getText().toString())) {
            mTextInputDevname.setError("设备备注不能为空");
            return false;
        } else {
            mTextInputDevname.setErrorEnabled(false);
        }
        return true;
    }


    /**
     * 把摄像头的信息存储到数据库
     */
    private void saveCamera() {

        CameraManager mCameraManager = CameraManager.getInstance(this);

        String name = mEditTextUsername.getText().toString().trim();
        String psw = mEditTextPassword.getText().toString().trim();
        String nickname = mEditTextDevname.getText().toString().trim();

        if (mCameraManager.isExist(uid)) {
            mCameraManager.deleteCameraByUID(uid);
        }

        mCamera = new CameraBean(uid, name, psw, nickname, null, null, 0, 0, 0, 0);

        AddCameraAsyncTask addCameraAsyncTask = new AddCameraAsyncTask(this, WEB_HEAD, mCamera, mMaterialDialog);
        addCameraAsyncTask.execute();
    }

    /**
     * 用于处理摄像头返回的数据
     */
    private Handler PPPPMsgHandler = new Handler() {
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            int msgParam = bundle.getInt(STR_MSG_PARAM);
            int msgType = msg.what;
            String did = bundle.getString(STR_DID);
            switch (msgType) {
                case ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS:
                    int resid;
                    switch (msgParam) {
                        case ContentCommon.PPPP_STATUS_CONNECTING:                 //正在连接   0
                            resid = R.string.pppp_status_connecting;
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED:             //连接失败    3
                            resid = R.string.pppp_status_connect_failed;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT:                    //断线      4
                            resid = R.string.pppp_status_disconnect;
                            tag = 0;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING:                 //已连接, 正在初始化  1
                            resid = R.string.pppp_status_initialing;
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_ID:                  //ID号无效         5
                            resid = R.string.pppp_status_invalid_id;
                            tag = 0;
                            mMaterialDialog.dismiss();
                            showJumpDialog();
                            break;
                        case ContentCommon.PPPP_STATUS_ON_LINE:                      //在线状态      2
                            resid = R.string.pppp_status_online;
                            tag = 1;
                            NativeCaller.StopPPPP(did);
                            saveCamera();
                            break;
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE:           //摄像机不在线     6
                            resid = R.string.device_not_on_line;
                            tag = 0;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT:               //连接超时      7
                            resid = R.string.pppp_status_connect_timeout;
                            tag = 0;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER:                 //密码错误    8
                            resid = R.string.pppp_status_pwd_error;
                            tag = 0;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            break;
                        default:
                            resid = R.string.pppp_status_unknown;
                            mMaterialDialog.dismiss();
                            mTvCameraInfo.setVisibility(View.VISIBLE);
                            mTvCameraInfo.setText(resid);
                            break;
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
     * 开启P2P连接
     */
    class StartPPPPThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                startCameraPPPP();
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

    private void showJumpDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.cancelable(false);
        builder.title("提示")
                .content("设备UID错误，将跳转到前一界面重新输入UID")
                .positiveText("确定").positiveColor(getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent intent = new Intent(mActivity, AddCameraActivity.class);
                        startActivity(intent);
                        mActivity.finish();
                    }
                })
                .show();
    }

    @Override
    public void BSMsgNotifyData(String did, int type, int param) {
        Bundle bundle = new Bundle();
        Message msg = PPPPMsgHandler.obtainMessage();
        msg.what = type;
        bundle.putInt(STR_MSG_PARAM, param);
        bundle.putString(STR_DID, did);
        msg.setData(bundle);
        PPPPMsgHandler.sendMessage(msg);
    }

    @Override
    public void BSSnapshotNotify(String did, byte[] bImage, int len) {

    }

    @Override
    public void callBackUserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {

    }

    @Override
    public void CameraStatus(String did, int status) {

    }

    /**
     * 活动销毁时关闭P2P连接
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeCaller.StopPPPP(uid);
    }

    /**
     * 点击输入框其他的地方就关闭输入法界面
     */
    public boolean onTouchEvent(MotionEvent event) {
        View view = ConfirmCameraActivity.this.getCurrentFocus();
        hideKeyboard(view);
        return super.onTouchEvent(event);
    }

    /**
     * 关闭输入法界面
     */
    private void hideKeyboard(View view) {
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
