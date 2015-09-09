package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.adapter.SearchListAdapter;
import com.jiazi.ipcamera.asyncTask.GetCameraAsyncTask;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.SystemValue;

import java.util.Map;

import vstc2.nativecaller.NativeCaller;

/**
 * 通过手动输入uid或者局域网搜索或者扫描二维码添加摄像头
 */
public class AddCameraActivity extends AppCompatActivity implements View.OnClickListener, BridgeService.AddCameraInterface {
    private static final int SEARCH_TIME = 3000;
    private final static int SCANNING_REQUEST_CODE = 1;       //请求码

    public static String CameraUrl = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=MonitorApi&a=getMinfo&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private Activity mActivity = this;
    private CoordinatorLayout mCoordinatorLayout;

    private ImageButton nextBtn;
    private ImageButton backBtn;
    private ImageButton mImageBtnClear;

    private TextInputLayout uidInput;
    private EditText uidEdittext;

    private CardView searchCameraCV;
    private CardView scanCodeCV;
    private CardView cloudCameraCV;

    private boolean isSearched;

    private SearchListAdapter mSearchListAdapter = null;

    private MaterialDialog mMaterialDialog = null;
    private MaterialDialog mLoadDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_add_camera);

        mSearchListAdapter = new SearchListAdapter(this);

        nextBtn = (ImageButton) findViewById(R.id.btn_next_step);
        nextBtn.setOnClickListener(this);
        backBtn = (ImageButton) findViewById(R.id.btn_cancel);
        backBtn.setOnClickListener(this);

        mImageBtnClear = (ImageButton) findViewById(R.id.imagebtn_clear);
        mImageBtnClear.setOnClickListener(this);

        searchCameraCV = (CardView) findViewById(R.id.cardview_search_camera);
        searchCameraCV.setOnClickListener(this);
        scanCodeCV = (CardView) findViewById(R.id.cardview_scan_code);
        scanCodeCV.setOnClickListener(this);
        cloudCameraCV = (CardView) findViewById(R.id.cardview_cloud_download);
        cloudCameraCV.setOnClickListener(this);

        uidInput = (TextInputLayout) findViewById(R.id.textInput_uid);
        uidEdittext = uidInput.getEditText();
        uidEdittext.clearFocus();

        BridgeService.setAddCameraInterface(this);
    }


    /**
     * 点击事件
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.cardview_search_camera) {
            SystemValue.deviceId = null;
            if (!isSearched) {
                isSearched = true;
                startSearch();
            } else {
                showScanResultDialog();
            }

        } else if (i == R.id.btn_next_step) {
            String uid = uidEdittext.getText().toString();
            boolean isExist = CameraManager.getInstance(this).isExist(uid);
            if (isExist) {             //如果摄像头已存在
                Toast.makeText(AddCameraActivity.this, "摄像头已存在", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(uid)) {
                uidInput.setError("摄像头UID不能为空");
            } else {
                Intent intent = new Intent(AddCameraActivity.this, ConfirmCameraActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }

        } else if (i == R.id.cardview_scan_code) {
            Intent intent = new Intent(AddCameraActivity.this, ScanQRActivity.class);
            startActivityForResult(intent, SCANNING_REQUEST_CODE);

        } else if (i == R.id.cardview_cloud_download) {
            showLoadDialog();
            GetCameraAsyncTask getCameraAsyncTask = new GetCameraAsyncTask(this, CameraUrl,mLoadDialog);
            getCameraAsyncTask.execute();
        } else if (i == R.id.btn_cancel) {
            AddCameraActivity.this.finish();

        } else if (i == R.id.imagebtn_clear) {
            uidEdittext.setText("");
        } else {
        }
    }

    private void showLoadDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        mLoadDialog = builder
                .cancelable(false)
                .title("提示")
                .content("正在加载，请稍候...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCANNING_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String uid = bundle.getString("result");
                uidEdittext.setText(uid);
            }
        }
    }


    /**
     * 显示扫描结果的对话框
     */
    private void showScanResultDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(getResources().getString(R.string.add_search_result))
                .positiveText(getResources().getString(R.string.refresh)).positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText(getResources().getString(R.string.str_cancel)).negativeColor(getResources().getColor(R.color.colorPrimary))
                .adapter(mSearchListAdapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        Map<String, Object> mapItem = (Map<String, Object>) mSearchListAdapter.getItemContent(i);
                        if (mapItem == null) {
                            return;
                        }

                        String strDID = (String) mapItem.get(ContentCommon.STR_CAMERA_ID);
                        uidEdittext.setText(strDID);
                        materialDialog.dismiss();
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        startSearch();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                    }
                })
                .show();
    }

    /**
     * 开始搜索设备
     */
    private void startSearch() {
        mSearchListAdapter.ClearAll();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        mMaterialDialog = builder.content(getString(R.string.searching_tip))
                .cancelable(false)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();

        // 执行扫描操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeCaller.StartSearch();
            }
        }).start();

        // 延迟SEARCH_TIME秒后执行updateThread
        updateListHandler.postDelayed(updateThread, SEARCH_TIME);
    }

    Runnable updateThread = new Runnable() {
        public void run() {
            NativeCaller.StopSearch();
            mMaterialDialog.dismiss();

            Message msg = updateListHandler.obtainMessage();
            msg.what = 1;
            updateListHandler.sendMessage(msg);
        }
    };

    /**
     * BridgeService callback
     **/
    @Override
    public void callBackSearchResultData(int cameraType, String strMac, String strName, String strDeviceID,
                                         String strIpAddr, int port) {
        if (!mSearchListAdapter.AddCamera(strMac, strName, strDeviceID)) {
            return;
        }
    }

    Handler updateListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mSearchListAdapter.notifyDataSetChanged();
                if (mSearchListAdapter.getCount() > 0) {
                    if (mActivity != null) {
                        showScanResultDialog();
                    }
                } else {
                    Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.add_search_no), Snackbar.LENGTH_SHORT).show();
                    isSearched = false;//
                }
            }
        }
    };

    @Override
    protected void onStop() {
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
        }
        NativeCaller.StopSearch();
        super.onStop();
    }

    /**
     * 点击输入框其他的地方就关闭输入法界面
     */
    public boolean onTouchEvent(MotionEvent event) {
        View view = AddCameraActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.onTouchEvent(event);
    }
}
