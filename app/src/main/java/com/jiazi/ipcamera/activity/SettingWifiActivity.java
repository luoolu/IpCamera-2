package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.jiazi.ipcamera.adapter.WifiScanListAdapter;
import com.jiazi.ipcamera.bean.WifiBean;
import com.jiazi.ipcamera.bean.WifiScanBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.ContentCommon;

import java.util.Timer;
import java.util.TimerTask;

import vstc2.nativecaller.NativeCaller;

/**
 * 摄像头Wifi设置功能
 */
public class SettingWifiActivity extends Activity implements OnClickListener, OnItemClickListener, BridgeService.WifiInterface {
    private String LOG_TAG = "SettingWifiActivity";
    private Context mContext;
    private String strDID;
    private String cameraName;
    private String cameraPwd;
    private boolean changeWifiFlag = false;// �ж��Ƿ���wifi
    private boolean successFlag = false;// ��ȡ�����õĽ��
    private static final int INITTIMEOUT = 30000;
    private final int END = 1;// wifi scan end flag
    private int result;
    private final int WIFIPARAMS = 1;// getwifi params
    private final int SCANPARAMS = 2;// scan wifi params
    private final int OVER = 3;// set wifi over
    private final int TIMEOUT = 4;
    private int CAMERAPARAM = 0xffffffff;// ����״̬
    // security
    private final int NO = 0;
    private final int WEP = 1;
    private final int WPA_PSK_AES = 2;
    private final int WPA_PSK_TKIP = 3;
    private final int WPA2_PSK_AES = 4;
    private final int WPA2_PSK_TKIP = 5;
    private Timer mTimerTimeOut;
    private boolean isTimerOver = false;
    private ImageButton btnCancel;
    private CardView mWifiCardView;
    private CardView mWifiListCardView;
    private ListView mListView;
    private PopupWindow popupWindow;
    private TextView tvName;
    private TextView tvPrompt;
    private TextView tvSafe;
    private EditText editPwd;
    private FloatingActionButton btnManager;
    private WifiBean wifiBean;
    private WifiScanListAdapter mAdapter;

    boolean wrapInScrollView = true;
    private boolean pswShowed = false;

    private TextView mTvWifiSsid;
    private TextView mTvWifiSignal;
    private TextView mTvWifiSec;
    private EditText mEtWifiPwd;
    private MaterialAnimatedSwitch mSwitchLockPwd;
    private MaterialDialog mWifiDialog;

    private MaterialDialog mScanDialog;

    /**
     * wifi getParams and Scaned
     **/
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WIFIPARAMS:
                    successFlag = true;
                    if (mMaterialDialog.isShowing()) {
                        mMaterialDialog.cancel();
                    }
                    if (wifiBean.getEnable() == 1) {
                        if (!TextUtils.isEmpty(wifiBean.getSsid())) {
                            tvName.setText(wifiBean.getSsid());
                            tvPrompt.setText(getResources().getString(
                                    R.string.connected));
                        } else {
                            tvName.setText(getResources().getString(
                                    R.string.wifi_no_safe));
                            tvPrompt.setText(getResources().getString(
                                    R.string.wifi_not_connected));
                        }

                        switch (wifiBean.getAuthtype()) {
                            case NO:
                                tvSafe.setText(getResources().getString(
                                        R.string.wifi_no_safe));
                                break;
                            case WEP:
                                tvSafe.setText("WEP");
                                break;
                            case WPA_PSK_AES:
                                tvSafe.setText("WPA_PSK(AES)");
                                break;
                            case WPA_PSK_TKIP:
                                tvSafe.setText("WPA_PSK(TKIP)");
                                break;
                            case WPA2_PSK_AES:
                                tvSafe.setText("WPA2_PSK(AES)");
                                break;
                            case WPA2_PSK_TKIP:
                                tvSafe.setText("WPA2_PSK(TKIP)");
                                break;
                            default:
                                break;
                        }

                    }
                    break;
                case SCANPARAMS:// wifi scan
                    Log.d(LOG_TAG, "handler  scan wifi");
                    if (mScanDialog.isShowing()) {
                        mScanDialog.cancel();
                        if (!isTimerOver) {
                            mTimerTimeOut.cancel();
                        }
                        mListView.setAdapter(mAdapter);
                        setListViewHeight();
                        mWifiListCardView.setVisibility(View.VISIBLE);
                        mListView.setVisibility(View.VISIBLE);
                        Log.d(LOG_TAG, "handler  scan wifi  2");
                    }
                    Log.d(LOG_TAG, "handler  scan wifi  3");
                    break;
                case OVER:// set over
                    successFlag = true;
                    if (result == 1) {
                        Log.d("tag", "over");
                        NativeCaller.PPPPRebootDevice(strDID);
                        Toast.makeText(
                                SettingWifiActivity.this,
                                getResources().getString(R.string.wifi_set_success),
                                Toast.LENGTH_LONG).show();
                        // Intent intent2 = new Intent(SettingWifiActivity.this,
                        // IpcamClientActivity.class);
                        // startActivity(intent2);
                        Intent intent2 = new Intent("myback");
                        sendBroadcast(intent2);
                        finish();
                    } else if (result == 0) {
                        showToast(R.string.wifi_set_failed);
                    }
                    break;
                case TIMEOUT:
                    if (mScanDialog.isShowing()) {
                        mScanDialog.cancel();
                    }
                    // showToast(R.string.wifi_scan_failed);
                    break;

                default:
                    break;
            }
        }
    };

    private void showToast(int i) {
        Toast.makeText(SettingWifiActivity.this, getResources().getString(i), Toast.LENGTH_SHORT).show();
    }

    /**
     * Listitem click
     **/
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
            mWifiDialog = builder.title("Wifi信息")
                    .customView(R.layout.setting_wifi_param, wrapInScrollView)
                    .positiveText("确认").positiveColor(getResources().getColor(R.color.colorPrimary))
                    .negativeText("取消").negativeColor(getResources().getColor(R.color.colorPrimary))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            setWifi();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialog.dismiss();
                        }
                    })
                    .show();
            View view = mWifiDialog.getCustomView();
            assignViews(view);        //Wifi对话框控件初始化
            mTvWifiSsid.setText(wifiBean.getSsid());
            mTvWifiSignal.setText(wifiBean.getDbm0() + "%");
            switch (wifiBean.getAuthtype()) {
                case NO:
                    mTvWifiSec.setText(getResources().getString(R.string.wifi_no_safe));
                    break;
                case WEP:
                    mTvWifiSec.setText("WEP");
                    break;
                case WPA_PSK_AES:
                    mTvWifiSec.setText("WPA_PSK(AES)");
                    break;
                case WPA_PSK_TKIP:
                    mTvWifiSec.setText("WPA_PSK(TKIP)");
                    break;
                case WPA2_PSK_AES:
                    mTvWifiSec.setText("WPA2_PSK(AES)");
                    break;
                case WPA2_PSK_TKIP:
                    mTvWifiSec.setText("WPA2_PSK(TKIP)");
                    break;
                default:
                    break;
            }

        }
    };
    private MaterialDialog mMaterialDialog;

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (!successFlag) {
                mMaterialDialog.dismiss();
                // showToast(R.string.wifi_getparams_failed);
            }
        }
    };

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);// �˳�����
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromOther();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settingwifi);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        mMaterialDialog = builder.content(getString(R.string.wifi_getparams))
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();
        mHandler.postDelayed(runnable, INITTIMEOUT);
        wifiBean = new WifiBean();
        mContext = this;
        findView();
        setListener();
        mAdapter = new WifiScanListAdapter(this);
        mListView.setOnItemClickListener(this);

        BridgeService.setWifiInterface(this);
        NativeCaller.PPPPGetSystemParams(strDID,
                ContentCommon.MSG_TYPE_GET_PARAMS);
        NativeCaller.PPPPGetSystemParams(strDID,
                ContentCommon.MSG_TYPE_GET_RECORD);
    }


    /**
     * wifi对话窗口控件初始化
     */
    private void assignViews(View v) {
        mTvWifiSsid = (TextView) v.findViewById(R.id.tv_wifi_ssid);
        mTvWifiSignal = (TextView) v.findViewById(R.id.tv_wifi_signal);
        mTvWifiSec = (TextView) v.findViewById(R.id.tv_wifi_sec);
        mEtWifiPwd = (EditText) v.findViewById(R.id.et_wifi_pwd);
        mSwitchLockPwd = (MaterialAnimatedSwitch) v.findViewById(R.id.switch_lock_pwd);
        mSwitchLockPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pswShowed) {
                    mEtWifiPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pswShowed = true;
                } else {
                    mEtWifiPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pswShowed = false;
                }
            }
        });
    }


    private void getDataFromOther() {
        Intent intent = getIntent();
        strDID = intent.getStringExtra(ContentCommon.STR_CAMERA_ID);
        cameraName = intent.getStringExtra(ContentCommon.STR_CAMERA_NAME);
        cameraPwd = intent.getStringExtra(ContentCommon.STR_CAMERA_PWD);
    }

    private void setListener() {
        btnManager.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void findView() {
        btnCancel = (ImageButton) findViewById(R.id.wifi_cancel);
        mWifiCardView = (CardView) findViewById(R.id.cardview_wifi);
        mWifiListCardView = (CardView) findViewById(R.id.cardview_wifilist);
        mListView = (ListView) findViewById(R.id.wifi_listview);
        tvName = (TextView) findViewById(R.id.wifi_tv_name);
        tvPrompt = (TextView) findViewById(R.id.wifi_tv_prompt);
        tvSafe = (TextView) findViewById(R.id.wifi_tv_safe);
        btnManager = (FloatingActionButton) findViewById(R.id.fab_wifi_manger);
        tvCameraName = (TextView) findViewById(R.id.tv_camera_setting);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.fab_wifi_manger) {
            mAdapter.clearWifi();
            mAdapter.notifyDataSetChanged();
            mWifiCardView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            NativeCaller.PPPPGetSystemParams(strDID,
                    ContentCommon.MSG_TYPE_WIFI_SCAN);
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            mScanDialog = builder.content(getResources().getString(R.string.wifi_scanning))
                    .progress(true, 0)
                    .cancelable(true)
                    .show();
            mScanDialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }

            });
            setTimeOut();

        } else if (i == R.id.wifi_cancel) {
            finish();
            overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);// �˳�����


        } else {
        }
    }

    private void setTimeOut() {

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                Log.d(LOG_TAG, "isTimeOver");
                isTimerOver = true;
                mHandler.sendEmptyMessage(TIMEOUT);
            }
        };
        mTimerTimeOut = new Timer();
        mTimerTimeOut.schedule(task, INITTIMEOUT);
    }

    private void setWifi() {
        if (changeWifiFlag) {
            String pwd = editPwd.getText().toString();
            if (wifiBean.getAuthtype() == NO) {
                wifiBean.setWpa_psk("");
                wifiBean.setKey1("");
            } else {
                if (!TextUtils.isEmpty(pwd)) {
                    if (wifiBean.getAuthtype() == WEP) {
                        wifiBean.setKey1(pwd);
                    } else {
                        wifiBean.setWpa_psk(pwd);
                    }
                } else {
                    showToast(R.string.pwd_no_empty);
                    return;
                }
            }
            try {
                NativeCaller.PPPPWifiSetting(wifiBean.getDid(),
                        wifiBean.getEnable(), wifiBean.getSsid(),
                        wifiBean.getChannel(), wifiBean.getMode(),
                        wifiBean.getAuthtype(), wifiBean.getEncryp(),
                        wifiBean.getKeyformat(), wifiBean.getDefkey(),
                        wifiBean.getKey1(), wifiBean.getKey2(),
                        wifiBean.getKey3(), wifiBean.getKey4(),
                        wifiBean.getKey1_bits(), wifiBean.getKey2_bits(),
                        wifiBean.getKey3_bits(), wifiBean.getKey4_bits(),
                        wifiBean.getWpa_psk());

            } catch (Exception e) {
                showToast(R.string.wifi_scan_failed);
                e.printStackTrace();
            }

        } else {
            showToast(R.string.wifi_notchange);
        }
        // Intent intent3 = new Intent(SettingWifiActivity.this,
        // MainActivity.class);
        // intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // startActivity(intent3);
    }

    private void setttingTimeOut() {
        successFlag = false;
        mHandler.postAtTime(settingRunnable, INITTIMEOUT);
    }

    private Runnable settingRunnable = new Runnable() {

        @Override
        public void run() {
            if (!successFlag) {
                showToast(R.string.wifi_set_failed);
            }
        }
    };
    private TextView tvCameraName;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        changeWifiFlag = true;
        WifiScanBean wifiScan = mAdapter.getWifiScan(position);
        wifiBean.setSsid(wifiScan.getSsid());
        wifiBean.setAuthtype(wifiScan.getSecurity());
        wifiBean.setChannel(wifiScan.getChannel());
        wifiBean.setDbm0(wifiScan.getDbm0());
        handler.sendEmptyMessage(1);

    }

    public void setListViewHeight() {
        ListAdapter adapter = mListView.getAdapter();
        if (adapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = adapter.getCount(); i < len; i++) { // listAdapter.getCount()������������Ŀ
            View listItem = adapter.getView(i, null, mListView);
            listItem.measure(0, 0); // ��������View �Ŀ��
            totalHeight += listItem.getMeasuredHeight(); // ͳ������������ܸ߶�
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = totalHeight
                + (mListView.getDividerHeight() * (adapter.getCount() - 1));
        mListView.setLayoutParams(params);
    }

    /**
     * BridgeService callback
     */
    @Override
    public void callBackWifiParams(String did, int enable, String ssid,
                                   int channel, int mode, int authtype, int encryp, int keyformat,
                                   int defkey, String key1, String key2, String key3, String key4,
                                   int key1_bits, int key2_bits, int key3_bits, int key4_bits,
                                   String wpa_psk) {
        Log.d("tag", "did:" + did + " enable:" + enable + " ssid:" + ssid
                + " channel:" + channel + " authtype:" + authtype + " encryp:"
                + encryp + " wpa_psk:" + wpa_psk);
        wifiBean.setDid(did);
        wifiBean.setEnable(1);// enable������ʱһ����1
        wifiBean.setSsid(ssid);
        wifiBean.setChannel(channel);
        wifiBean.setMode(0);// 0
        wifiBean.setAuthtype(authtype);// security ��--��������
        wifiBean.setEncryp(0);// 0
        wifiBean.setKeyformat(0);// 0
        wifiBean.setDefkey(0);// 0
        wifiBean.setKey1(key1);// ""wep
        wifiBean.setKey2("");// ""
        wifiBean.setKey3("");// ""
        wifiBean.setKey4("");// ""
        wifiBean.setKey1_bits(0);// 0
        wifiBean.setKey2_bits(0);// 0
        wifiBean.setKey3_bits(0);// 0
        wifiBean.setKey4_bits(0);// 0
        wifiBean.setWpa_psk(wpa_psk);// ����
        Log.d(LOG_TAG, wifiBean.toString());
        mHandler.sendEmptyMessage(WIFIPARAMS);
    }

    /**
     * BridgeService callback
     */
    @Override
    public void callBackWifiScanResult(String did, String ssid, String mac,
                                       int security, int dbm0, int dbm1, int mode, int channel, int bEnd) {
        Log.d(LOG_TAG, "ssid:" + ssid + " mac:" + mac + " security:" + security
                + " dbm0��" + dbm0 + " dbm1:" + dbm1 + " mode:" + mode
                + " channel:" + channel + " bEnd:" + bEnd);
        Log.d(LOG_TAG, "bEnd=" + bEnd);
        if (bEnd != END) {
            Log.d(LOG_TAG, "��");
            WifiScanBean bean = new WifiScanBean();
            bean.setDid(did);
            bean.setSsid(ssid);
            bean.setChannel(channel);
            bean.setSecurity(security);
            bean.setDbm0(dbm0);
            bean.setMac(mac);
            bean.setMode(mode);
            bean.setDbm1(dbm1);
            mAdapter.addWifiScan(bean);
        } else {
            Log.d(LOG_TAG, "���� bEnd=" + bEnd);
            mHandler.sendEmptyMessage(SCANPARAMS);
        }
    }

    /**
     * BridgeService callback
     */
    @Override
    public void callBackSetSystemParamsResult(String did, int paramType,
                                              int result) {
        Log.d("tag", "result:" + result);
        this.result = result;
        mHandler.sendEmptyMessage(OVER);
    }

    /**
     * BridgeService callback
     */
    @Override
    public void callBackPPPPMsgNotifyData(String did, int type, int param) {
        if (strDID.equals(did)) {
            if (ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS == type) {
                CAMERAPARAM = param;
            }
        }
    }
}
