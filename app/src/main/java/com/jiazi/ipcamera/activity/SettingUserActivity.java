package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.jiazi.ipcamera.asyncTask.ChangeCameraAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.ContentCommon;

import vstc2.nativecaller.NativeCaller;

/**
 * 摄像头用户名密码设置功能
 */
public class SettingUserActivity extends Activity implements OnCheckedChangeListener, OnClickListener, BridgeService.UserInterface {

    private static final String WEB_HEAD = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=updateMinfo&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private boolean successFlag = false;
    private int CAMERAPARAM = 0xffffffff;
    private final int TIMEOUT = 3000;
    private final int FAILED = 0;
    private final int SUCCESS = 1;
    private final int PARAMS = 3;
    private String strDID;//camera id
    private String cameraName;
    private String operatorName = "";
    private String operatorPwd = "";
    private String visitorName = "";
    private String visitorPwd = "";
    private String adminName = "";
    private String adminPwd = "";
    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutPwd;
    private EditText editName;
    private EditText editPwd;
    private MaterialAnimatedSwitch mUserPwdSwitch;
    private ImageButton btnOk;
    private ImageButton btnCancel;
    private MaterialDialog mMaterialDialog;

    private boolean pswShowed = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FAILED://set failed
                    showToast(R.string.user_set_failed);
                    break;
                case SUCCESS://set success
                    showToast(R.string.user_set_success);
                    NativeCaller.PPPPRebootDevice(strDID);

                    Log.d("info", "user:" + adminName + " pwd:" + adminPwd);
                    final Intent intent = new Intent(ContentCommon.STR_CAMERA_INFO_RECEIVER);
                    intent.putExtra(ContentCommon.STR_CAMERA_NAME, cameraName);
                    intent.putExtra(ContentCommon.STR_CAMERA_ID, strDID);
                    intent.putExtra(ContentCommon.STR_CAMERA_USER, adminName);
                    intent.putExtra(ContentCommon.STR_CAMERA_PWD, adminPwd);
                    intent.putExtra(ContentCommon.STR_CAMERA_OLD_ID, strDID);
                    intent.putExtra(ContentCommon.CAMERA_OPTION, ContentCommon.CHANGE_CAMERA_USER);
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            sendBroadcast(intent);
                        }
                    }, 3000);
                    finish();

                    break;
                case PARAMS://get user params
                    successFlag = true;
                    if (mMaterialDialog.isShowing()) {
                        mMaterialDialog.cancel();
                    }
                    editName.setText(adminName);
                    editPwd.setText(adminPwd);
                    break;

                default:
                    break;
            }
        }
    };

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (!successFlag) {
                successFlag = false;
                mMaterialDialog.dismiss();
//					showToast(R.string.user_getparams_failed);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromOther();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settinguser);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        mMaterialDialog = builder.content(getString(R.string.user_getparams))
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();
        mHandler.postDelayed(runnable, TIMEOUT);
        findView();
        setLisetener();
        BridgeService.setUserInterface(this);
        NativeCaller.PPPPGetSystemParams(strDID, ContentCommon.MSG_TYPE_GET_PARAMS);
    }

    private void setLisetener() {
        mUserPwdSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pswShowed) {
                    editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pswShowed = true;
                } else {
                    editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pswShowed = false;
                }
            }
        });
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        MyTextWatch myNameTextWatch = new MyTextWatch(R.id.edit_name);
        editName.addTextChangedListener(myNameTextWatch);
        MyTextWatch myPwdTextWatch = new MyTextWatch(R.id.edit_pwd);
        editPwd.addTextChangedListener(myPwdTextWatch);
    }

    private void getDataFromOther() {
        Intent intent = getIntent();
        strDID = intent.getStringExtra(ContentCommon.STR_CAMERA_ID);
        cameraName = intent.getStringExtra(ContentCommon.STR_CAMERA_NAME);
        adminName = "admin";
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
        super.onPause();
    }

    private void findView() {
        textInputLayoutName = (TextInputLayout) findViewById(R.id.textInput_name);
        textInputLayoutPwd = (TextInputLayout) findViewById(R.id.textInput_pwd);
        editName = textInputLayoutName.getEditText();
        editPwd = textInputLayoutPwd.getEditText();
        mUserPwdSwitch = (MaterialAnimatedSwitch) findViewById(R.id.switch_show_pwd);
        btnOk = (ImageButton) findViewById(R.id.user_ok);
        btnCancel = (ImageButton) findViewById(R.id.user_cancel);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.user_ok) {
            setUser();

        } else if (i == R.id.user_cancel) {
            finish();

        } else {
        }
    }

    /**
     * 发送指令给摄像头修改用户名密码
     */
    private void setUser() {
        if (successFlag) {
            if (TextUtils.isEmpty(adminName)) {
                showToast(R.string.user_name_no_empty);
                return;
            }

            if (TextUtils.isEmpty(adminPwd)) {
                showToast(R.string.pwd_no_empty);
                return;
            }

            Log.d("info", "adminName:" + adminName + " adminPwd:" + adminPwd);
            NativeCaller.PPPPUserSetting(strDID, visitorName, visitorPwd, operatorName, operatorPwd, adminName, adminPwd);

            CameraManager mCameraManager = CameraManager.getInstance(this);
            CameraBean mCamera = mCameraManager.getCameraByUID(strDID);
            mCamera.setName(adminName);
            mCamera.setPsw(adminPwd);
            mCameraManager.changeDevice(mCamera);
            ChangeCameraAsyncTask changeCameraAsyncTask = new ChangeCameraAsyncTask(this, WEB_HEAD, strDID, adminName, adminPwd);
            changeCameraAsyncTask.execute();
            Intent intent = new Intent(SettingUserActivity.this, CameraListActivity.class);
            startActivity(intent);
            finish();
        } else {
            showToast(R.string.user_set_failed);
        }
    }

    private Runnable settingRunnable = new Runnable() {

        @Override
        public void run() {
            if (!successFlag) {
                showToast(R.string.user_set_failed);
            }
        }
    };

    /**
     * BridgeService Feedback execute
     **/
    public void CallBack_UserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {
        Log.d("info", " did:" + did + " user1:" + user1 + " pwd1:" + pwd1 + " user2:" + user2 + " pwd2:" + pwd2 + " user3:" + user3 + " pwd3:" + pwd3);
        adminName = user3;
        adminPwd = pwd3;
        mHandler.sendEmptyMessage(PARAMS);
    }

    /**
     * BridgeService Feedback execute
     **/
    public void CallBack_SetSystemParamsResult(String did, int paramType, int result) {
        Log.d("info", "result:" + result + " paramType:" + paramType);
        mHandler.sendEmptyMessage(result);
    }

    /**
     * BridgeService Feedback execute
     **/
    public void setPPPPMsgNotifyData(String did, int type, int param) {
        if (strDID.equals(did)) {
            if (ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS == type) {
                CAMERAPARAM = param;
            }
        }
    }

    private class MyTextWatch implements TextWatcher {
        private int id;

        public MyTextWatch(int id) {
            this.id = id;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String result = s.toString();
            if (id == R.id.edit_name) {
                adminName = result;

            } else if (id == R.id.edit_pwd) {
                Log.i("info", "result:" + result);
                adminPwd = result;

            } else {
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int rid) {
        Toast.makeText(this, getResources().getString(rid), Toast.LENGTH_LONG).show();
    }

    @Override
    public void callBackUserParams(String did, String user1, String pwd1,
                                   String user2, String pwd2, String user3, String pwd3) {
        // TODO Auto-generated method stub
        Log.e("用户信息", "管理员名称" + adminName + "管理员密码" + adminPwd);
        adminName = user3;
        adminPwd = pwd3;
        operatorName = user2;
        operatorPwd = pwd2;
        mHandler.sendEmptyMessage(PARAMS);
    }

    @Override
    public void callBackSetSystemParamsResult(String did, int paramType,
                                              int result) {
        // TODO Auto-generated method stub
        mHandler.sendEmptyMessage(result);
    }

    @Override
    public void callBackPPPPMsgNotifyData(String did, int type, int param) {
        // TODO Auto-generated method stub

    }

}
