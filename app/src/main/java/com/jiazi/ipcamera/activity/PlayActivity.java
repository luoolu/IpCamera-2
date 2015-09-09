package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.TrafficStats;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.adapter.ViewPagerAdapter;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.AudioPlayer;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.CustomAudioRecorder;
import com.jiazi.ipcamera.utils.CustomBuffer;
import com.jiazi.ipcamera.utils.CustomBufferData;
import com.jiazi.ipcamera.utils.CustomBufferHead;
import com.jiazi.ipcamera.utils.CustomVideoRecord;
import com.jiazi.ipcamera.utils.DatabaseUtil;
import com.jiazi.ipcamera.utils.MyRender;
import com.jiazi.ipcamera.utils.SystemValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import vstc2.nativecaller.NativeCaller;


/**
 * 实时播放功能
 */
public class PlayActivity extends Activity implements OnTouchListener, OnGestureListener, OnClickListener, BridgeService.PlayInterface, CustomAudioRecorder.AudioRecordResult {

    private static final String LOG_TAG = "PlayActivity";
    private static final int AUDIO_BUFFER_START_CODE = 0xff00ff;
    //surfaceView控件
    private GLSurfaceView playSurface = null;

    //视频数据
    private byte[] videodata = null;
    private int videoDataLen = 0;
    public int nVideoWidths = 0;
    public int nVideoHeights = 0;

    private View progressView = null;
    private boolean bProgress = true;
    private GestureDetector gt = new GestureDetector(this);
    private final int BRIGHT = 1;//亮度标志
    private final int CONTRAST = 2;//对比度标志
    private int nResolution = 0;//分辨率值
    private int nBrightness = 0;//亮度值
    private int nContrast = 0;//对比度

    private boolean bInitCameraParam = false;
    private boolean bManualExit = false;
    private TextView textosd = null;
    private String strName = null;
    private String strDID = null;
    private View osdView = null;
    private boolean bDisplayFinished = true;
    private CustomBuffer AudioBuffer = null;
    private AudioPlayer audioPlayer = null;
    private boolean bAudioStart = false;

    private boolean isLeftRight = false;
    private boolean isUpDown = false;

    private DatabaseUtil dbUtil;

    private boolean isHorizontalMirror = false;
    private boolean isVerticalMirror = false;
    private boolean isUpDownPressed = false;
    private boolean isShowtoping = false;
    private ImageView videoViewPortrait, videoViewLandscape;
    private ImageView videoViewStandard;
    //顶部控件声明
    private HorizontalScrollView bottomView;
    private ImageButton ptzAudio, ptztalk, ptzDefaultSet, ptzBrightness, ptzContrast, ptzTake_photos, ptzTake_vodeo, ptzResolutoin, preset;
    private int nStreamCodecType;//分辨率格式


    private PopupWindow controlWindow;//设备方向控制提示控件
    private PopupWindow mPopupWindowProgress;//进度条控件
    private PopupWindow presetBitWindow;//预置位面板
    private PopupWindow resolutionPopWindow;//分辨率面板
    //上下左右提示文本
    private TextView control_item;
    //正在控制设备
    private boolean isControlDevice = false;

    private String stqvga = "qvga";
    private String stvga = "vga";
    private String stqvga1 = "qvga1";
    private String stvga1 = "vga1";
    private String stp720 = "p720";
    private String sthigh = "high";
    private String stmiddle = "middle";
    private String stmax = "max";

    //预位置设置
    private Button[] btnLeft = new Button[16];
    private Button[] btnRigth = new Button[16];
    private ViewPager prePager;
    private List<View> listViews;
    //分辨率标识符
    private boolean ismax = false;
    private boolean ishigh = false;
    private boolean isp720 = false;
    private boolean ismiddle = false;
    private boolean isqvga1 = false;
    private boolean isvga1 = false;
    private boolean isqvga = false;
    private boolean isvga = false;

    private Animation showAnim;
    private boolean isTakepic = false;
    private boolean isTalking = false;//是否在说话
    private boolean isMcriophone = false;
    //视频录像方法
    private CustomVideoRecord myvideoRecorder;
    public boolean isH264 = false;//是否是H264格式标志
    private boolean isTakeVideo = false;
    private long videotime = 0;// 录每张图片的时间

    private Animation dismissAnim;
    private int timeTag = 0;
    private int timeOne = 0;
    private int timeTwo = 0;
    private ImageButton button_back;
    private BitmapDrawable drawable = null;
    private boolean bAudioRecordStart = false;
    //送话器
    private CustomAudioRecorder customAudioRecorder;

    private MyRender myRender;

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    //显示顶部菜单
    private void showTop() {
        if (isShowtoping) {
            isShowtoping = false;
            topbg.setVisibility(View.GONE);
            topbg.startAnimation(dismissTopAnim);
        } else {
            isShowtoping = true;
            topbg.setVisibility(View.VISIBLE);
            topbg.startAnimation(showTopAnim);
        }
    }

    //默认视频参数
    private void defaultVideoParams() {
        nBrightness = 1;
        nContrast = 128;
        NativeCaller.PPPPCameraControl(strDID, 1, 0);
        NativeCaller.PPPPCameraControl(strDID, 2, 128);
        showToast(R.string.ptz_default_vedio_params);
    }

    private void showToast(int i) {
        Toast.makeText(PlayActivity.this, i, Toast.LENGTH_SHORT).show();
    }


    //设置视频可见
    private void setViewVisible() {
        if (bProgress) {
            bProgress = false;
            progressView.setVisibility(View.INVISIBLE);
            osdView.setVisibility(View.VISIBLE);
            getCameraParams();
        }
    }

    int disPlaywidth;
    private Bitmap mBmp;
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 1 || msg.what == 2) {
                setViewVisible();
            }
            if (!isPTZPrompt) {
                isPTZPrompt = true;
                showToast(R.string.ptz_control);
            }
            int width = getWindowManager().getDefaultDisplay().getWidth();
            int height = getWindowManager().getDefaultDisplay().getHeight();
            switch (msg.what) {
                case 1: // h264
                {
                    if (reslutionlist.size() == 0) {
                        if (nResolution == 0) {
                            ismax = true;
                            ismiddle = false;
                            ishigh = false;
                            isp720 = false;
                            isqvga1 = false;
                            isvga1 = false;
                            addReslution(stmax, ismax);
                        } else if (nResolution == 1) {
                            ismax = false;
                            ismiddle = false;
                            ishigh = true;
                            isp720 = false;
                            isqvga1 = false;
                            isvga1 = false;
                            addReslution(sthigh, ishigh);
                        } else if (nResolution == 2) {
                            ismax = false;
                            ismiddle = true;
                            ishigh = false;
                            isp720 = false;
                            isqvga1 = false;
                            isvga1 = false;
                            addReslution(stmiddle, ismiddle);
                        } else if (nResolution == 3) {
                            ismax = false;
                            ismiddle = false;
                            ishigh = false;
                            isp720 = true;
                            isqvga1 = false;
                            isvga1 = false;
                            addReslution(stp720, isp720);
                            nResolution = 3;
                        } else if (nResolution == 4) {
                            ismax = false;
                            ismiddle = false;
                            ishigh = false;
                            isp720 = false;
                            isqvga1 = false;
                            isvga1 = true;
                            addReslution(stvga1, isvga1);
                        } else if (nResolution == 5) {
                            ismax = false;
                            ismiddle = false;
                            ishigh = false;
                            isp720 = false;
                            isqvga1 = true;
                            isvga1 = false;
                            addReslution(stqvga1, isqvga1);
                        }
                    } else {
                        if (reslutionlist.containsKey(strDID)) {
                            getReslution();
                        } else {
                            if (nResolution == 0) {
                                ismax = true;
                                ismiddle = false;
                                ishigh = false;
                                isp720 = false;
                                isqvga1 = false;
                                isvga1 = false;
                                addReslution(stmax, ismax);
                            } else if (nResolution == 1) {
                                ismax = false;
                                ismiddle = false;
                                ishigh = true;
                                isp720 = false;
                                isqvga1 = false;
                                isvga1 = false;
                                addReslution(sthigh, ishigh);
                            } else if (nResolution == 2) {
                                ismax = false;
                                ismiddle = true;
                                ishigh = false;
                                isp720 = false;
                                isqvga1 = false;
                                isvga1 = false;
                                addReslution(stmiddle, ismiddle);
                            } else if (nResolution == 3) {
                                ismax = false;
                                ismiddle = false;
                                ishigh = false;
                                isp720 = true;
                                isqvga1 = false;
                                isvga1 = false;
                                addReslution(stp720, isp720);
                                nResolution = 3;
                            } else if (nResolution == 4) {
                                ismax = false;
                                ismiddle = false;
                                ishigh = false;
                                isp720 = false;
                                isqvga1 = false;
                                isvga1 = true;
                                addReslution(stvga1, isvga1);
                            } else if (nResolution == 5) {
                                ismax = false;
                                ismiddle = false;
                                ishigh = false;
                                isp720 = false;
                                isqvga1 = true;
                                isvga1 = false;
                                addReslution(stqvga1, isqvga1);
                            }
                        }

                    }

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                width, width * 3 / 4);
                        lp.gravity = Gravity.CENTER;
                        playSurface.setLayoutParams(lp);
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                width, height);
                        lp.gravity = Gravity.CENTER;
                        playSurface.setLayoutParams(lp);
                    }
                    myRender.writeSample(videodata, nVideoWidths, nVideoHeights);
                }
                break;
                case 2: // JPEG
                {
                    if (reslutionlist.size() == 0) {
                        if (nResolution == 1) {
                            isvga = true;
                            isqvga = false;
                            addReslution(stvga, isvga);
                        } else if (nResolution == 0) {
                            isqvga = true;
                            isvga = false;
                            addReslution(stqvga, isqvga);
                        }
                    } else {
                        if (reslutionlist.containsKey(strDID)) {
                            getReslution();
                        } else {
                            if (nResolution == 1) {
                                isvga = true;
                                isqvga = false;
                                addReslution(stvga, isvga);
                            } else if (nResolution == 0) {
                                isqvga = true;
                                isvga = false;
                                addReslution(stqvga, isqvga);
                            }
                        }
                    }
                    mBmp = BitmapFactory.decodeByteArray(videodata, 0,
                            videoDataLen);
                    if (mBmp == null) {
                        bDisplayFinished = true;
                        return;
                    }
                    if (isTakepic) {
                        takePicture(mBmp);
                        isTakepic = false;
                    }
                    nVideoWidths = mBmp.getWidth();
                    nVideoHeights = mBmp.getHeight();

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // Bitmap
                        Bitmap bitmap = Bitmap.createScaledBitmap(mBmp, width,
                                width * 3 / 4, true);
                        videoViewLandscape.setVisibility(View.GONE);
                        videoViewPortrait.setVisibility(View.VISIBLE);
                        videoViewPortrait.setImageBitmap(bitmap);

                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Bitmap bitmap = Bitmap.createScaledBitmap(mBmp, width,
                                height, true);
                        videoViewPortrait.setVisibility(View.GONE);
                        videoViewLandscape.setVisibility(View.VISIBLE);
                        videoViewLandscape.setImageBitmap(bitmap);
                    }

                }
                break;
                default:
                    break;
            }
            if (msg.what == 1 || msg.what == 2) {
                bDisplayFinished = true;
            }
        }

    };

    private void getCameraParams() {

        NativeCaller.PPPPGetSystemParams(strDID,
                ContentCommon.MSG_TYPE_GET_CAMERA_PARAMS);
    }

    private Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(),
                        R.string.pppp_status_disconnect, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getDataFromOther();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.play);
        strName = SystemValue.deviceName;
        strDID = SystemValue.deviceId;
        disPlaywidth = getWindowManager().getDefaultDisplay().getWidth();
        findView();                //用于控件初始化
        AudioBuffer = new CustomBuffer();
        audioPlayer = new AudioPlayer(AudioBuffer);
        customAudioRecorder = new CustomAudioRecorder(this);
        myvideoRecorder = new CustomVideoRecord(this, strDID);
        BridgeService.setPlayInterface(this);
        NativeCaller.StartPPPPLivestream(strDID, 10, 1);//确保不能重复start

        getCameraParams();     //获取摄像头参数
        dismissTopAnim = AnimationUtils.loadAnimation(this,
                R.anim.ptz_top_anim_dismiss);
        showTopAnim = AnimationUtils.loadAnimation(this,
                R.anim.ptz_top_anim_show);
        showAnim = AnimationUtils.loadAnimation(this,
                R.anim.ptz_otherset_anim_show);
        dismissAnim = AnimationUtils.loadAnimation(this,
                R.anim.ptz_otherset_anim_dismiss);

        myRender = new MyRender(playSurface);
        playSurface.setRenderer(myRender);

        lastTotalRxBytes = getTotalRxBytes();
        lastTimeStamp = System.currentTimeMillis();
        new Timer().schedule(task, 1000, 2000);

    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    private void showNetSpeed() {

        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        msg.obj = String.valueOf(speed) + " kb/s";

        mNetworkHandler.sendMessage(msg);//更新界面
    }

    private Handler mNetworkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String networkSpeed = (String) msg.obj;
            mNetworkSpeedText.setText(networkSpeed);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPopupWindowProgress != null && mPopupWindowProgress.isShowing()) {
            mPopupWindowProgress.dismiss();
        }
        if (resolutionPopWindow != null && resolutionPopWindow.isShowing()) {
            resolutionPopWindow.dismiss();
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showSureDialog();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!bProgress) {
                showTop();
                showBottom();
            } else {
                showSureDialog();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void setResolution(int Resolution) {
        NativeCaller.PPPPCameraControl(strDID, 16, Resolution);
    }


    //控件初始化
    private void findView() {
        //方向控制提示框
        initControlDailog();
        //视频渲染画面控件
        playSurface = (GLSurfaceView) findViewById(R.id.mysurfaceview);
        playSurface.setOnTouchListener(this);
        playSurface.setLongClickable(true);//确保手势识别正确工作

        button_back = (ImageButton) findViewById(R.id.login_top_back);
        button_back.setOnClickListener(this);
        videoViewPortrait = (ImageView) findViewById(R.id.vedioview);
        videoViewStandard = (ImageView) findViewById(R.id.vedioview_standard);

        progressView = (View) findViewById(R.id.progressLayout);
        //顶部菜单
        topbg = (RelativeLayout) findViewById(R.id.top_bg);
        osdView = (View) findViewById(R.id.osdlayout);
        //显示设备名称
        textosd = (TextView) findViewById(R.id.textosd);
        textosd.setText("设置");
        textosd.setVisibility(View.VISIBLE);
        textosd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showTop();
                showBottom();
            }
        });

        ptzHoriMirror2 = (ImageButton) findViewById(R.id.ptz_hori_mirror);
        ptzVertMirror2 = (ImageButton) findViewById(R.id.ptz_vert_mirror);
        ptzHoriTour2 = (ImageButton) findViewById(R.id.ptz_hori_tour);
        ptzVertTour2 = (ImageButton) findViewById(R.id.ptz_vert_tour);
        ptzHoriMirror2.setOnClickListener(this);
        ptzVertMirror2.setOnClickListener(this);
        ptzHoriTour2.setOnClickListener(this);
        ptzVertTour2.setOnClickListener(this);

        //底部菜单  可水平滑动
        bottomView = (HorizontalScrollView) findViewById(R.id.bottom_view);

        ptztalk = (ImageButton) findViewById(R.id.ptz_talk);
        ptzAudio = (ImageButton) findViewById(R.id.ptz_audio);
        ptzTake_photos = (ImageButton) findViewById(R.id.ptz_take_photos);
        ptzTake_vodeo = (ImageButton) findViewById(R.id.ptz_take_videos);
        ptzDefaultSet = (ImageButton) findViewById(R.id.ptz_default_set);
        ptzBrightness = (ImageButton) findViewById(R.id.ptz_brightness);
        ptzContrast = (ImageButton) findViewById(R.id.ptz_contrast);
        ptzResolutoin = (ImageButton) findViewById(R.id.ptz_resolution);
        preset = (ImageButton) findViewById(R.id.preset);
        mNetworkSpeedText = (TextView) findViewById(R.id.network_speed);

        ptztalk.setOnClickListener(this);
        ptzAudio.setOnClickListener(this);
        ptzTake_photos.setOnClickListener(this);
        ptzTake_vodeo.setOnClickListener(this);
        ptzBrightness.setOnClickListener(this);
        ptzContrast.setOnClickListener(this);
        ptzResolutoin.setOnClickListener(this);
        ptzDefaultSet.setOnClickListener(this);
        preset.setOnClickListener(this);


    }

    private boolean isDown = false;
    private boolean isSecondDown = false;
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (!isDown) {
            x1 = event.getX();
            y1 = event.getY();
            isDown = true;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                originalScale = getScale();
                break;
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs((x1 - x2)) < 25 && Math.abs((y1 - y2)) < 25) {

                    if (resolutionPopWindow != null
                            && resolutionPopWindow.isShowing()) {
                        resolutionPopWindow.dismiss();
                    }

                    if (mPopupWindowProgress != null
                            && mPopupWindowProgress.isShowing()) {
                        mPopupWindowProgress.dismiss();
                    }
                    if (!isSecondDown) {
                        if (!bProgress) {
                            showTop();
                            showBottom();
                        }
                    }
                    isSecondDown = false;
                }
                x1 = 0;
                x2 = 0;
                y1 = 0;
                y2 = 0;
                isDown = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isSecondDown = true;
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                y2 = event.getY();

                if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 0f) {

                    }
                }
        }

        return gt.onTouchEvent(event);
    }

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private int mode = NONE;
    private float oldDist;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float mMaxZoom = 2.0f;
    float mMinZoom = 0.3125f;
    float originalScale;
    float baseValue;
    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    private Matrix mDisplayMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];

    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > mMaxZoom) {
            scale = mMaxZoom;
        } else if (scale < mMinZoom) {
            scale = mMinZoom;
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;
        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        videoViewStandard.setScaleType(ImageView.ScaleType.MATRIX);
        videoViewStandard.setImageMatrix(getImageViewMatrix());
    }

    protected Matrix getImageViewMatrix() {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }

    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getScale() {
        return getScale(mSuppMatrix);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        } catch (Exception e) {
        }
        return 0;
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }

    private final int MINLEN = 80;//最小间距
    private RelativeLayout topbg;
    private Animation showTopAnim;
    private Animation dismissTopAnim;
    private ImageButton ptzHoriMirror2;
    private ImageButton ptzVertMirror2;
    private ImageButton ptzHoriTour2;
    private ImageButton ptzVertTour2;
    private TextView mNetworkSpeedText;
    private boolean isPTZPrompt;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float x1 = e1.getX();
        float x2 = e2.getX();
        float y1 = e1.getY();
        float y2 = e2.getY();

        float xx = x1 > x2 ? x1 - x2 : x2 - x1;
        float yy = y1 > y2 ? y1 - y2 : y2 - y1;

        if (xx > yy) {
            if ((x1 > x2) && (xx > MINLEN)) {// right
                if (!isControlDevice)
                    new ControlDeviceTask(ContentCommon.CMD_PTZ_RIGHT).execute();

            } else if ((x1 < x2) && (xx > MINLEN)) {// left
                if (!isControlDevice)
                    new ControlDeviceTask(ContentCommon.CMD_PTZ_LEFT).execute();
            }

        } else {
            if ((y1 > y2) && (yy > MINLEN)) {// down
                if (!isControlDevice)
                    new ControlDeviceTask(ContentCommon.CMD_PTZ_DOWN).execute();
            } else if ((y1 < y2) && (yy > MINLEN)) {// up
                if (!isControlDevice)
                    new ControlDeviceTask(ContentCommon.CMD_PTZ_UP).execute();
            }

        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


    public void showSureDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(getResources().getString(R.string.exit_show))
                .content(R.string.exit_play_show)
                .positiveText(R.string.str_ok).positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText(R.string.str_cancel).negativeColor(getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        PlayActivity.this.finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.login_top_back) {
            bManualExit = true;
            if (isTakeVideo == true) {
                showToast(R.string.eixt_show_toast);
            } else {
                showSureDialog();
            }

        } else if (i == R.id.ptz_hori_mirror) {
            if (isHorizontalMirror) {
                ptzHoriMirror2.setBackgroundColor(0x00ffffff);
                isHorizontalMirror = false;
                NativeCaller.PPPPCameraControl(strDID, 5, ContentCommon.CMD_PTZ_ORIGINAL);

            } else {
                isHorizontalMirror = true;
                ptzHoriMirror2.setBackgroundColor(0xff0044aa);
                NativeCaller.PPPPCameraControl(strDID, 5, ContentCommon.CMD_PTZ_HORIZONAL_MIRROR);
            }

        } else if (i == R.id.ptz_vert_mirror) {
            if (isVerticalMirror) {
                isVerticalMirror = false;
                ptzVertMirror2.setBackgroundColor(0x00ffffff);
                NativeCaller.PPPPCameraControl(strDID, 5, ContentCommon.CMD_PTZ_ORIGINAL);
            } else {
                isVerticalMirror = true;
                ptzVertMirror2.setBackgroundColor(0xff0044aa);
                NativeCaller.PPPPCameraControl(strDID, 5, ContentCommon.CMD_PTZ_VERTICAL_MIRROR);
            }

        } else if (i == R.id.ptz_hori_tour) {
            if (isLeftRight) {
                ptzHoriTour2.setBackgroundColor(0x000044aa);
                isLeftRight = false;
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_LEFT_RIGHT_STOP);
            } else {
                ptzHoriTour2.setBackgroundColor(0xff0044aa);
                isLeftRight = true;
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_LEFT_RIGHT);
            }

        } else if (i == R.id.ptz_vert_tour) {
            if (isUpDown) {
                ptzVertTour2.setBackgroundColor(0x000044aa);
                isUpDown = false;
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_UP_DOWN_STOP);
            } else {
                ptzVertTour2.setBackgroundColor(0xff0044aa);
                isUpDown = true;
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_UP_DOWN);
            }

        } else if (i == R.id.ptz_talk) {
            goMicroPhone();

        } else if (i == R.id.ptz_take_videos) {
            goTakeVideo();

        } else if (i == R.id.ptz_take_photos) {
            dismissBrightAndContrastProgress();
            if (existSdcard()) {// 判断sd卡是否存在
                isTakepic = true;
            } else {
                showToast(R.string.ptz_takepic_save_fail);
            }

        } else if (i == R.id.ptz_audio) {
            goAudio();

        } else if (i == R.id.ptz_brightness) {
            if (mPopupWindowProgress != null
                    && mPopupWindowProgress.isShowing()) {
                mPopupWindowProgress.dismiss();
                mPopupWindowProgress = null;
            }
            setBrightOrContrast(BRIGHT);

        } else if (i == R.id.ptz_contrast) {
            if (mPopupWindowProgress != null
                    && mPopupWindowProgress.isShowing()) {
                mPopupWindowProgress.dismiss();
                mPopupWindowProgress = null;
            }
            setBrightOrContrast(CONTRAST);

        } else if (i == R.id.ptz_resolution) {
            showResolutionPopWindow();   //将画质调节的二级界面显示出来

        } else if (i == R.id.preset) {
            presetBitWindow();

        } else if (i == R.id.ptz_resolution_jpeg_qvga) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            nResolution = 1;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_jpeg_vga) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            nResolution = 0;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_h264_qvga) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = false;
            ismiddle = false;
            ishigh = false;
            isp720 = false;
            isqvga1 = true;
            isvga1 = false;
            addReslution(stqvga1, isqvga1);
            nResolution = 5;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_h264_vga) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = false;
            ismiddle = false;
            ishigh = false;
            isp720 = false;
            isqvga1 = false;
            isvga1 = true;
            addReslution(stvga1, isvga1);
            nResolution = 4;
            setResolution(nResolution);


        } else if (i == R.id.ptz_resolution_h264_720p) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = false;
            ismiddle = false;
            ishigh = false;
            isp720 = true;
            isqvga1 = false;
            isvga1 = false;
            addReslution(stp720, isp720);
            nResolution = 3;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_h264_middle) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = false;
            ismiddle = true;
            ishigh = false;
            isp720 = false;
            isqvga1 = false;
            isvga1 = false;
            addReslution(stmiddle, ismiddle);
            nResolution = 2;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_h264_high) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = false;
            ismiddle = false;
            ishigh = true;
            isp720 = false;
            isqvga1 = false;
            isvga1 = false;
            addReslution(sthigh, ishigh);
            nResolution = 1;
            setResolution(nResolution);

        } else if (i == R.id.ptz_resolution_h264_max) {
            dismissBrightAndContrastProgress();
            resolutionPopWindow.dismiss();
            ismax = true;
            ismiddle = false;
            ishigh = false;
            isp720 = false;
            isqvga1 = false;
            isvga1 = false;
            addReslution(stmax, ismax);
            nResolution = 0;
            setResolution(nResolution);

        } else if (i == R.id.ptz_default_set) {
            dismissBrightAndContrastProgress();
            defaultVideoParams();

        }
    }

    private void dismissBrightAndContrastProgress() {
        if (mPopupWindowProgress != null && mPopupWindowProgress.isShowing()) {
            mPopupWindowProgress.dismiss();
            mPopupWindowProgress = null;
        }
    }

    private void showBottom() {
        if (isUpDownPressed) {
            isUpDownPressed = false;
            bottomView.startAnimation(dismissAnim);
            bottomView.setVisibility(View.GONE);
        } else {
            isUpDownPressed = true;
            bottomView.startAnimation(showAnim);
            bottomView.setVisibility(View.VISIBLE);
        }
    }

    /*
     *异步控制方向
     */
    private class ControlDeviceTask extends AsyncTask<Void, Void, Integer> {
        private int type;

        public ControlDeviceTask(int type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if (type == ContentCommon.CMD_PTZ_RIGHT) {
                control_item.setText(R.string.right);
            } else if (type == ContentCommon.CMD_PTZ_LEFT) {
                control_item.setText(R.string.left);
            } else if (type == ContentCommon.CMD_PTZ_UP) {
                control_item.setText(R.string.up);
            } else if (type == ContentCommon.CMD_PTZ_DOWN) {
                control_item.setText(R.string.down);
            }
            if (controlWindow != null && controlWindow.isShowing())
                controlWindow.dismiss();

            if (controlWindow != null && !controlWindow.isShowing())
                controlWindow.showAtLocation(playSurface, Gravity.CENTER, 0, 0);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            isControlDevice = true;
            if (type == ContentCommon.CMD_PTZ_RIGHT) {
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_RIGHT);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_RIGHT_STOP);
            } else if (type == ContentCommon.CMD_PTZ_LEFT) {
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_LEFT);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_LEFT_STOP);
            } else if (type == ContentCommon.CMD_PTZ_UP) {
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_UP);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_UP_STOP);
            } else if (type == ContentCommon.CMD_PTZ_DOWN) {
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_DOWN);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NativeCaller.PPPPPTZControl(strDID, ContentCommon.CMD_PTZ_DOWN_STOP);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            isControlDevice = false;
            if (controlWindow != null && controlWindow.isShowing())
                controlWindow.dismiss();
        }

    }

    /*
     * 上下左右滑动屏幕出现进度条跟文字提示
     */
    private void initControlDailog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.control_device_view, null);
        control_item = (TextView) view.findViewById(R.id.textView1_play);
        controlWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        controlWindow.setBackgroundDrawable(new ColorDrawable(0));
        controlWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                controlWindow.dismiss();
            }
        });
        controlWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    controlWindow.dismiss();
                }
                return false;
            }
        });
    }

    /*
     * 16个预置位设置面板
     */
    private void presetBitWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.preset_view, null);
        initViewPager(view);
        presetBitWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        presetBitWindow.setAnimationStyle(R.style.AnimationPreview);
        presetBitWindow.setFocusable(true);
        presetBitWindow.setOutsideTouchable(true);
        presetBitWindow.setBackgroundDrawable(new ColorDrawable(0));
        presetBitWindow.showAtLocation(playSurface, Gravity.CENTER, 0, 0);

    }

    private void initViewPager(View view) {
        final TextView left = (TextView) view.findViewById(R.id.text_pre_left);
        final TextView rigth = (TextView) view.findViewById(R.id.text_pre_right);
        left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                left.setTextColor(Color.BLUE);
                rigth.setTextColor(0xffffffff);
                prePager.setCurrentItem(0);
            }
        });
        rigth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                rigth.setTextColor(Color.BLUE);
                left.setTextColor(0xffffffff);
                prePager.setCurrentItem(1);
            }
        });

        prePager = (ViewPager) view.findViewById(R.id.vPager);
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        View view1 = mInflater.inflate(R.layout.popuppreset, null);
        View view2 = mInflater.inflate(R.layout.popuppreset, null);
        btnLeft[0] = (Button) view1.findViewById(R.id.pre1);
        btnRigth[0] = (Button) view2.findViewById(R.id.pre1);
        btnLeft[1] = (Button) view1.findViewById(R.id.pre2);
        btnRigth[1] = (Button) view2.findViewById(R.id.pre2);
        btnLeft[2] = (Button) view1.findViewById(R.id.pre3);
        btnRigth[2] = (Button) view2.findViewById(R.id.pre3);
        btnLeft[3] = (Button) view1.findViewById(R.id.pre4);
        btnRigth[3] = (Button) view2.findViewById(R.id.pre4);
        btnLeft[4] = (Button) view1.findViewById(R.id.pre5);
        btnRigth[4] = (Button) view2.findViewById(R.id.pre5);
        btnLeft[5] = (Button) view1.findViewById(R.id.pre6);
        btnRigth[5] = (Button) view2.findViewById(R.id.pre6);
        btnLeft[6] = (Button) view1.findViewById(R.id.pre7);
        btnRigth[6] = (Button) view2.findViewById(R.id.pre7);
        btnLeft[7] = (Button) view1.findViewById(R.id.pre8);
        btnRigth[7] = (Button) view2.findViewById(R.id.pre8);
        btnLeft[8] = (Button) view1.findViewById(R.id.pre9);
        btnRigth[8] = (Button) view2.findViewById(R.id.pre9);
        btnLeft[9] = (Button) view1.findViewById(R.id.pre10);
        btnRigth[9] = (Button) view2.findViewById(R.id.pre10);
        btnLeft[10] = (Button) view1.findViewById(R.id.pre11);
        btnRigth[10] = (Button) view2.findViewById(R.id.pre11);
        btnLeft[11] = (Button) view1.findViewById(R.id.pre12);
        btnRigth[11] = (Button) view2.findViewById(R.id.pre12);
        btnLeft[12] = (Button) view1.findViewById(R.id.pre13);
        btnRigth[12] = (Button) view2.findViewById(R.id.pre13);
        btnLeft[13] = (Button) view1.findViewById(R.id.pre14);
        btnRigth[13] = (Button) view2.findViewById(R.id.pre14);
        btnLeft[14] = (Button) view1.findViewById(R.id.pre15);
        btnRigth[14] = (Button) view2.findViewById(R.id.pre15);
        btnLeft[15] = (Button) view1.findViewById(R.id.pre16);
        btnRigth[15] = (Button) view2.findViewById(R.id.pre16);
        listViews.add(view1);
        listViews.add(view2);
        prePager.setAdapter(new ViewPagerAdapter(listViews));
        prePager.setCurrentItem(0);
        prePager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 0) {
                    left.setTextColor(Color.BLUE);
                    rigth.setTextColor(0xffffffff);
                } else {
                    rigth.setTextColor(Color.BLUE);
                    left.setTextColor(0xffffffff);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        PresetListener listener = new PresetListener();
        for (int i = 0; i < 16; i++) {
            btnLeft[i].setOnClickListener(listener);
            btnRigth[i].setOnClickListener(listener);
        }
    }

    //预位置设置监听
    private class PresetListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            int id = arg0.getId();
            presetBitWindow.dismiss();
            int currIndex = prePager.getCurrentItem();
            if (id == R.id.pre1) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 31);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 30);
                }

            } else if (id == R.id.pre2) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 33);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 32);
                }

            } else if (id == R.id.pre3) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 35);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 34);
                }


                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 37);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 36);
                }

            } else if (id == R.id.pre4) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 37);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 36);
                }

            } else if (id == R.id.pre5) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 39);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 38);
                }

            } else if (id == R.id.pre6) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 41);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 40);
                }

            } else if (id == R.id.pre7) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 43);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 42);
                }

            } else if (id == R.id.pre8) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 45);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 44);
                }

            } else if (id == R.id.pre9) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 47);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 46);
                }

            } else if (id == R.id.pre10) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 49);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 48);
                }

            } else if (id == R.id.pre11) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 51);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 50);
                }

            } else if (id == R.id.pre12) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 53);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 52);
                }

            } else if (id == R.id.pre13) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 55);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 54);
                }

            } else if (id == R.id.pre14) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 57);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 56);
                }

            } else if (id == R.id.pre15) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 59);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 58);
                }

            } else if (id == R.id.pre16) {
                if (currIndex == 0) {
                    NativeCaller.PPPPPTZControl(strDID, 61);
                } else {
                    NativeCaller.PPPPPTZControl(strDID, 60);
                }

            }
        }
    }

    //判断sd卡是否存在
    private boolean existSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    // 拍照
    private void takePicture(final Bitmap bmp) {
        new Thread() {
            public void run() {
                savePicToSDcard(bmp);
            }
        }.start();
    }

    /*
     * 保存到本地
     *
     */
    private synchronized void savePicToSDcard(final Bitmap bmp) {
        String strDate = getStrDate();
        FileOutputStream fos = null;
        try {
            File div = new File(Environment.getExternalStorageDirectory(),
                    "ipcamera/takepic");
            if (!div.exists()) {
                div.mkdirs();
            }
            File file = new File(div, strDate + "_" + strDID + ".jpg");
            fos = new FileOutputStream(file);
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {         //bmp图片压缩成jpg
                fos.flush();
                dbUtil = new DatabaseUtil(this);
                dbUtil.open();
                dbUtil.createVideoOrPic(strDID, file.getAbsolutePath(), DatabaseUtil.TYPE_PICTURE, "2323");
                Log.e("Picture", file.getAbsolutePath());
                dbUtil.close();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showToast(R.string.ptz_takepic_ok);
                    }
                });
            }
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.ptz_takepic_fail);
                }
            });
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fos = null;
            }
        }
    }

    //时间格式
    private String getStrDate() {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String strDate = f.format(d);
        return strDate;
    }

    /*
     * 录像
     */
    private void goTakeVideo() {
        dismissBrightAndContrastProgress();
        if (isTakeVideo) {
            showToast(R.string.ptz_takevideo_end);
            NativeCaller.RecordLocal(strDID, 0);
            isTakeVideo = false;
            ptzTake_vodeo.setImageResource(R.drawable.ptz_takevideo);
            myvideoRecorder.stopRecordVideo();
        } else {
            isTakeVideo = true;
            showToast(R.string.ptz_takevideo_begin);
            videotime = (new Date()).getTime();
            ptzTake_vodeo.setImageResource(R.drawable.ptz_takevideo_pressed);
            NativeCaller.RecordLocal(strDID, 1);
            if (isH264) {
                myvideoRecorder.startRecordVideo(1);
            } else {
                myvideoRecorder.startRecordVideo(2);
            }
        }
    }

    //讲话
    private void StartTalk() {
        if (customAudioRecorder != null) {
            customAudioRecorder.StartRecord();
            NativeCaller.PPPPStartTalk(strDID);
        }
    }

    //停止讲话
    private void StopTalk() {
        if (customAudioRecorder != null) {
            customAudioRecorder.StopRecord();
            NativeCaller.PPPPStopTalk(strDID);
        }
    }

    //监听
    private void StartAudio() {
        synchronized (this) {
            AudioBuffer.ClearAll();
            audioPlayer.AudioPlayStart();
            NativeCaller.PPPPStartAudio(strDID);
        }
    }

    //停止监听
    private void StopAudio() {
        synchronized (this) {
            audioPlayer.AudioPlayStop();
            AudioBuffer.ClearAll();
            NativeCaller.PPPPStopAudio(strDID);
        }
    }

    /**
     * 监听
     */
    private void goAudio() {
        dismissBrightAndContrastProgress();
        if (bAudioStart) {
            isTalking = false;
            bAudioStart = false;
            ptzAudio.setImageResource(R.drawable.ptz_audio_off);
            StopAudio();            //停止接收音频
        } else {
            isTalking = true;
            bAudioStart = true;
            ptzAudio.setImageResource(R.drawable.ptz_audio_on);
            StartAudio();              //开始接收音频
        }
    }

    /**
     * 对讲
     */
    private void goMicroPhone() {
        dismissBrightAndContrastProgress();
        if (bAudioRecordStart) {
            isMcriophone = false;
            bAudioRecordStart = false;
            ptztalk.setImageResource(R.drawable.ptz_microphone_off);
            StopTalk();
        } else {
            isMcriophone = true;
            bAudioRecordStart = true;
            ptztalk.setImageResource(R.drawable.ptz_microphone_on);
            StartTalk();
        }
    }

    /*
     * 分辨率设置
     */
    private void showResolutionPopWindow() {

        if (resolutionPopWindow != null && resolutionPopWindow.isShowing()) {
            return;
        }
        if (nStreamCodecType == ContentCommon.PPPP_STREAM_TYPE_JPEG) {
            // jpeg
            LinearLayout layout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.ptz_resolution_jpeg, null);
            TextView qvga = (TextView) layout
                    .findViewById(R.id.ptz_resolution_jpeg_qvga);
            TextView vga = (TextView) layout
                    .findViewById(R.id.ptz_resolution_jpeg_vga);
            if (reslutionlist.size() != 0) {
                getReslution();
            }
            if (isvga) {
                vga.setTextColor(Color.RED);
            }
            if (isqvga) {
                qvga.setTextColor(Color.RED);
            }
            qvga.setOnClickListener(this);
            vga.setOnClickListener(this);
            resolutionPopWindow = new PopupWindow(layout, 100,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            int x_begin = getWindowManager().getDefaultDisplay().getWidth() / 6;
            int y_begin = ptzResolutoin.getTop();
            resolutionPopWindow.showAtLocation(findViewById(R.id.play),
                    Gravity.BOTTOM | Gravity.RIGHT, x_begin, y_begin);

        } else {
            // h264
            LinearLayout layout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.ptz_resolution_h264, null);
            TextView qvga1 = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_qvga);
            TextView vga1 = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_vga);
            TextView p720 = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_720p);
            TextView middle = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_middle);
            TextView high = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_high);
            TextView max = (TextView) layout
                    .findViewById(R.id.ptz_resolution_h264_max);

            if (reslutionlist.size() != 0) {
                getReslution();
            }
            if (ismax) {
                max.setTextColor(Color.RED);
            }
            if (ishigh) {
                high.setTextColor(Color.RED);
            }
            if (ismiddle) {
                middle.setTextColor(Color.RED);
            }
            if (isqvga1) {
                qvga1.setTextColor(Color.RED);
            }
            if (isvga1) {
                vga1.setTextColor(Color.RED);
            }
            if (isp720) {
                p720.setTextColor(Color.RED);
            }
            high.setOnClickListener(this);
            middle.setOnClickListener(this);
            max.setOnClickListener(this);
            qvga1.setOnClickListener(this);
            vga1.setOnClickListener(this);
            p720.setOnClickListener(this);
            resolutionPopWindow = new PopupWindow(layout, 100,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            int oreation = display.getOrientation();
            int x_begin = getWindowManager().getDefaultDisplay().getWidth() / 6;
            int y_begin = ptzResolutoin.getTop();
            resolutionPopWindow.showAtLocation(findViewById(R.id.play),
                    Gravity.BOTTOM | Gravity.RIGHT, x_begin, y_begin + 60);

        }

    }

    /**
     * 获取reslution
     */
    public static Map<String, Map<Object, Object>> reslutionlist = new HashMap<String, Map<Object, Object>>();

    /**
     * 增加reslution
     */
    private void addReslution(String mess, boolean isfast) {
        if (reslutionlist.size() != 0) {
            if (reslutionlist.containsKey(strDID)) {
                reslutionlist.remove(strDID);
            }
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(mess, isfast);
        reslutionlist.put(strDID, map);
    }

    private void getReslution() {
        if (reslutionlist.containsKey(strDID)) {
            Map<Object, Object> map = reslutionlist.get(strDID);
            if (map.containsKey("qvga")) {
                isqvga = true;
            } else if (map.containsKey("vga")) {
                isvga = true;
            } else if (map.containsKey("qvga1")) {
                isqvga1 = true;
            } else if (map.containsKey("vga1")) {
                isvga1 = true;
            } else if (map.containsKey("p720")) {
                isp720 = true;
            } else if (map.containsKey("high")) {
                ishigh = true;
            } else if (map.containsKey("middle")) {
                ismiddle = true;
            } else if (map.containsKey("max")) {
                ismax = true;
            }
        }
    }

    /*
     * @param type
     * 亮度饱和对比度
     */
    private void setBrightOrContrast(final int type) {

        if (!bInitCameraParam) {
            return;
        }
        int width = getWindowManager().getDefaultDisplay().getWidth();
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.brightprogress, null);
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.brightseekBar1);
        seekBar.setMax(255);
        switch (type) {
            case BRIGHT:
                seekBar.setProgress(nBrightness);
                break;
            case CONTRAST:
                seekBar.setProgress(nContrast);
                break;
            default:
                break;
        }
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                switch (type) {
                    case BRIGHT:// 亮度
                        nBrightness = progress;
                        NativeCaller.PPPPCameraControl(strDID, BRIGHT, nBrightness);
                        break;
                    case CONTRAST:// 对比度
                        nContrast = progress;
                        NativeCaller.PPPPCameraControl(strDID, CONTRAST, nContrast);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress,
                                          boolean arg2) {

            }
        });
        mPopupWindowProgress = new PopupWindow(layout, width / 2, 180);
        mPopupWindowProgress.showAtLocation(findViewById(R.id.play),
                Gravity.TOP, 0, 0);

    }

    @Override
    protected void onDestroy() {
        NativeCaller.StopPPPPLivestream(strDID);
        StopAudio();
        StopTalk();
        if (myvideoRecorder != null) {
            myvideoRecorder.stopRecordVideo();
        }
        super.onDestroy();
    }

    /***
     * BridgeService callback 视频参数回调
     **/
    @Override
    public void callBackCameraParamNotify(String did, int resolution,
                                          int brightness, int contrast, int hue, int saturation, int flip) {

        nBrightness = brightness;
        nContrast = contrast;
        nResolution = resolution;
        bInitCameraParam = true;
    }

    /***
     * BridgeService callback 视频数据流回调
     **/
    @Override
    public void callBaceVideoData(byte[] videobuf, int h264Data, int len, int width, int height) {
        if (!bDisplayFinished)
            return;
        bDisplayFinished = false;
        videodata = videobuf;
        videoDataLen = len;
        Message msg = new Message();
        if (h264Data == 1) { // H264
            nVideoWidths = width;
            nVideoHeights = height;
            if (isTakepic) {
                isTakepic = false;
                byte[] rgb = new byte[width * height * 2];
                NativeCaller.YUV4202RGB565(videobuf, rgb, width, height);
                ByteBuffer buffer = ByteBuffer.wrap(rgb);
                mBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                mBmp.copyPixelsFromBuffer(buffer);
                takePicture(mBmp);
            }
            isH264 = true;
            msg.what = 1;
        } else { // JPEG
            msg.what = 2;
        }
        mHandler.sendMessage(msg);
    }

    /***
     * BridgeService callback
     **/

    @Override
    public void callBackMessageNotify(String did, int msgType, int param) {
        if (bManualExit)
            return;

        if (msgType == ContentCommon.PPPP_MSG_TYPE_STREAM) {
            nStreamCodecType = param;
            return;
        }

        if (msgType != ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS) {
            return;
        }

        if (!did.equals(strDID)) {
            return;
        }

        Message msg = new Message();
        msg.what = 1;
        msgHandler.sendMessage(msg);
    }

    /***
     * BridgeService callback
     **/
    @Override
    public void callBackAudioData(byte[] pcm, int len) {
        if (!audioPlayer.isAudioPlaying()) {
            return;
        }
        CustomBufferHead head = new CustomBufferHead();
        CustomBufferData data = new CustomBufferData();
        head.length = len;
        head.startcode = AUDIO_BUFFER_START_CODE;
        data.head = head;
        data.data = pcm;
        AudioBuffer.addData(data);
    }

    /***
     * BridgeService callback
     **/
    @Override
    public void callBackH264Data(byte[] h264, int type, int size) {
        if (isTakeVideo) {
            Date date = new Date();
            long time = date.getTime();
            int tspan = (int) (time - videotime);
            videotime = time;
            if (videoRecorder != null) {
                videoRecorder.VideoRecordData(type, h264, size, 0, tspan);
            }
        }
    }


    //对讲数据
    @Override
    public void AudioRecordData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (bAudioRecordStart && len > 0) {
            NativeCaller.PPPPTalkAudioData(strDID, data, len);
        }
    }

    //定义录像接口
    public void setVideoRecord(VideoRecorder videoRecorder) {
        this.videoRecorder = videoRecorder;
    }

    public VideoRecorder videoRecorder;

    public interface VideoRecorder {
        abstract public void VideoRecordData(int type, byte[] videodata,
                                             int width, int height, int time);
    }
}
