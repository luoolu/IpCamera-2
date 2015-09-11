package com.jiazi.ipcamera.asyncTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.activity.CameraActivity;
import com.jiazi.ipcamera.activity.ShowMapActivity;
import com.jiazi.ipcamera.adapter.DeviceAdapter;
import com.jiazi.ipcamera.bean.BluetoothdeviceBean;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.fragment.CameraFragment;
import com.jiazi.ipcamera.utils.CameraManager;
import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取手环地图并且在上面标注摄像头监控范围以及蓝牙设备坐标点
 */
public class GetMapAsyncTask extends AsyncTask<String, Void, Bitmap> {

    public static final String webUrl = "http://test.jiazi-it.com/spyscan/index.php?m=Home&c=PosApi&a=getDevicePos&token=96f86412b37fe9665c5ec4c3fad04f0f";

    private Activity mActivity;
    private ImageView mImageView;
    private MaterialDialog mDialog;
    private ListView mListView;
    private LayoutInflater mLayoutInflater;
    private List<CameraBean> mCameraBeanList;
    private List<BluetoothdeviceBean> mBluetoothdeviceBeanList;
    private List<BluetoothdeviceBean> mAlarmdeviceBeanList;
    private ArrayList<Integer> xPositions;
    private ArrayList<Integer> yPositions;
    private CardView mCardView;
    private int topPos;
    private int bottomPos;
    private int leftPos;
    private int rightPos;
    private double bitmapWidth;
    private double bitmapHeight;
    private double scaleFactor;        //图片缩放倍数


    public GetMapAsyncTask(Activity mActivity, ImageView mImageView, MaterialDialog dialog,
                           ListView mListView, LayoutInflater mInflater, CardView mCardView) {
        this.mActivity = mActivity;
        this.mImageView = mImageView;
        mDialog = dialog;
        this.mListView = mListView;
        this.mLayoutInflater = mInflater;
        this.mCardView = mCardView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap bmp = null;
        mBluetoothdeviceBeanList = new ArrayList<BluetoothdeviceBean>();
        mAlarmdeviceBeanList = new ArrayList<BluetoothdeviceBean>();
        mCameraBeanList = new ArrayList<CameraBean>();
        xPositions = new ArrayList<Integer>();
        yPositions = new ArrayList<Integer>();
        getBluetoothdevicePos(webUrl);            //后台进程连接服务器并且读取蓝牙设备信息
        String path = Environment.getExternalStorageDirectory().getPath();
        String filename = path + "/ipcamera/map/map.jpg";
        File file = new File(filename);
        Log.i("GetMapAsyncTask", file.getAbsolutePath());
        long length = file.length();       //取得图片文件的大小
        if (length < 200000) {    //如果地图图片文件小于一定值则删除
            file.delete();
        }
        if (!file.exists()) {       //检查地图图片是否已经存在本地
            bmp = getMapPic(getMapUrl());
            savePicToSDcard(bmp);
        } else {
            bmp = BitmapFactory.decodeFile(filename);
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            bitmapWidth = bitmap.getWidth() - 16.25 - 21.67;       //图片宽度再减去白色区域宽度
            bitmapHeight = bitmap.getHeight() - 16.25 - 19.86;     //图片高度再减去白色区域高度
            mImageView.setImageBitmap(bitmap);
            getImagePos(bitmap.getWidth(), bitmap.getHeight());
            Bitmap bmp = null;
            bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);             //画布不允许直接修改图片,所以要先copy
            Canvas canvas = new Canvas(bmp);
            drawBluetoothDevice(canvas);
            mCameraBeanList = CameraManager.getInstance(mActivity).getCameras();  //取得摄像头的数目
            if (mCameraBeanList.size() != 0) {
                for (int i = 0; i < mCameraBeanList.size(); i++) {
                    CameraBean camera = mCameraBeanList.get(i);
                    float xStart = camera.getxStart();
                    float xStop = camera.getxStop();
                    float yStart = camera.getyStart();
                    float yStop = camera.getyStop();
                    bmp = drawMap(bmp, xStart, xStop, yStart, yStop);
                }
            }
            mImageView.setImageBitmap(bmp);                              //设置图片
            if (mDialog != null) {
                mDialog.dismiss();
            }
            if (mAlarmdeviceBeanList.size() != 0 && mCameraBeanList.size() != 0) {       //如果有报警的蓝牙设备
                checkAlarmLocation();
            }

        } else {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
        mCardView.setVisibility(View.VISIBLE);
        DeviceAdapter mAdapter = new DeviceAdapter(mActivity, mBluetoothdeviceBeanList, mLayoutInflater);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 根据坐标绘制矩形以及标注蓝牙设备
     */
    public Bitmap drawMap(Bitmap bitmap, float xStart, float xStop, float yStart, float yStop) {
        int xStartLoc = (int) (bitmapWidth / 16.56 * xStart + 16.25);
        int xStopLoc = (int) (bitmapWidth / 16.56 * xStop + 16.25);
        int yStartLoc = (int) (bitmapHeight / 16.70 * yStart + 16.25);
        int yStopLoc = (int) (bitmapHeight / 16.70 * yStop + 16.25);
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);             //画布不允许直接修改图片,所以要先copy
        Canvas canvas = new Canvas(bmp);
        /**
         * 绘制矩形的画笔
         */
        Paint rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.5f);            //设置线宽
        rectPaint.setAlpha(0);                     //设置为透明
        if (!(xStart == 0 && xStop == 0 && yStart == 0 && yStop == 0)) {       //不描绘无监控范围的摄像头
            canvas.drawRect(new Rect(xStartLoc, yStartLoc, xStopLoc, yStopLoc), rectPaint);      //绘制矩形
        }

        /**
         * 数据返回给ShowMapActivity做触摸判断
         */
        ShowMapActivity.topPos.add((int) ((yStartLoc * scaleFactor) + topPos));
        ShowMapActivity.bottomPos.add((int) ((yStopLoc * scaleFactor) + topPos));
        ShowMapActivity.leftPos.add((int) (xStartLoc * scaleFactor));
        ShowMapActivity.rightPos.add((int) (xStopLoc * scaleFactor));
        ShowMapActivity.isPosSet = true;
        return bmp;
    }

    private void drawBluetoothDevice(Canvas canvas) {
        for (int i = 0; i < mBluetoothdeviceBeanList.size(); i++) {
            BluetoothdeviceBean mBluetoothdevice = mBluetoothdeviceBeanList.get(i);
            float xPos = Float.parseFloat(mBluetoothdevice.getxPos());
            float yPos = Float.parseFloat(mBluetoothdevice.getyPos());
            int xPoint = (int) (bitmapWidth / 16.56 * xPos + 16.25);
            int yPoint = (int) (bitmapHeight / 16.70 * yPos + 16.25);
            String type = mBluetoothdevice.getType();
            String id = mBluetoothdevice.getId();
            if (type.equals("报警")) {
                mAlarmdeviceBeanList.add(mBluetoothdevice);
            }
            drawCircle(canvas, xPoint, yPoint, type, id);
        }
    }

    private void drawCircle(Canvas canvas, int xPoint, int yPoint, String type, String id) {
        /**
         * 绘制坐标点的画笔
         */
        Paint cirPaint = new Paint();
        if (type.equals("报警")) {
            cirPaint.setColor(Color.RED);
        } else if (type.equals("良好")) {
            cirPaint.setColor(Color.GREEN);
        }
        xPoint = checkOverlay(xPoint, yPoint);
        cirPaint.setAntiAlias(true);     // 去除画笔的锯齿效果
        canvas.drawCircle(xPoint, yPoint, 15, cirPaint);    // 小圆
        xPositions.add(xPoint);
        yPositions.add(yPoint);

        /**
         * 绘制蓝牙设备的id文本
         */
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(20);
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();              //绘制文本对象
        int baseline = yPoint + (fontMetrics.bottom - fontMetrics.top) / 2 - 6;
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(id, xPoint, baseline, textPaint);     //绘制文本
    }

    /**
     * 获取ImageView的参数
     */
    private void getImagePos(double bitmapWidth, double bitmapHeight) {
        int[] position = new int[2];
        mImageView.getLocationOnScreen(position);
        topPos = position[1];          //取得图片的顶端位置
        double ivWidth = mImageView.getRight() - mImageView.getLeft();
        scaleFactor = ivWidth / bitmapWidth;
        double ivHeight = bitmapHeight * scaleFactor;
        bottomPos = (int) (topPos + ivHeight);   //取得图片的底端位置
        leftPos = mImageView.getLeft();        //取得图片的左端位置
        rightPos = mImageView.getRight();      //取得图片的右端位置
        Log.i("GetMapAsyncTask", "top:" + topPos + "bottom:" + bottomPos + "left:" + leftPos + "right:" + rightPos);
    }

    /**
     * 检查报警的蓝牙设备是否在监控范围内
     */
    public void checkAlarmLocation() {
        for (int i = 0; i < mCameraBeanList.size(); i++) {
            CameraBean camera = mCameraBeanList.get(i);
            float xStart = camera.getxStart();
            float xStop = camera.getxStop();
            float yStart = camera.getyStart();
            float yStop = camera.getyStop();
            int xStartLoc = (int) (bitmapWidth / 16.56 * xStart + 16.25);
            int xStopLoc = (int) (bitmapWidth / 16.56 * xStop + 16.25);
            int yStartLoc = (int) (bitmapHeight / 16.70 * yStart + 16.25);
            int yStopLoc = (int) (bitmapHeight / 16.70 * yStop + 16.25);
            for (int j = 0; j < mAlarmdeviceBeanList.size(); j++) {
                BluetoothdeviceBean mAlarmdevice = mAlarmdeviceBeanList.get(j);
                float xPos = Float.parseFloat(mAlarmdevice.getxPos());
                float yPos = Float.parseFloat(mAlarmdevice.getyPos());
                int xPoint = (int) (bitmapWidth / 16.56 * xPos + 16.25);
                int yPoint = (int) (bitmapHeight / 16.70 * yPos + 16.25);
                if (xPoint > xStartLoc && xPoint < xStopLoc && yPoint > yStartLoc && yPoint < yStopLoc) {
                    if (!ShowMapActivity.isShowing) {
                        showConfirmDialog(camera);
                    }
                }
            }
        }
    }

    /**
     * 检查蓝牙设备是否重叠
     */
    private int checkOverlay(int x, int y) {
        for (int i = 0; i < xPositions.size(); i++) {
            int xPosition = xPositions.get(i);
            int yPosition = yPositions.get(i);
            int x1 = Math.abs(xPosition - x);       //两点之间x坐标差值
            int y1 = Math.abs(yPosition - y);       //两点之间y坐标差值
            double distance = Math.sqrt(x1 * x1 + y1 * y1);      //两点之间的距离
            if (distance < 30) {
                if (x - xPosition > 0) {
                    x = x + 30;
                } else {
                    x = x - 30;
                }
            }
        }
        return x;
    }

    private void showConfirmDialog(final CameraBean camera) {
        ShowMapActivity.isShowing = true;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title("警告")
                .content("监控范围内存在报警信号，跳转到摄像头: " + camera.getNickname())
                .positiveText("确定").positiveColor(mActivity.getResources().getColor(R.color.colorPrimary))
                .negativeText("取消").negativeColor(mActivity.getResources().getColor(R.color.colorPrimary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent intent = new Intent(mActivity, CameraActivity.class);
                        String uid = camera.getDid();
                        String username = camera.getName();
                        String password = camera.getPsw();
                        String devName = camera.getNickname();
                        intent.putExtra("uid", uid);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        intent.putExtra("devName", devName);
                        CameraFragment.fromMapActivity = true;
                        mActivity.startActivity(intent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                        ShowMapActivity.isShowing = false;
                    }
                })
                .show();
    }

    /**
     * 从服务器获取蓝牙设备的信息
     */
    private void getBluetoothdevicePos(String weburl) {
        String result = HttpUtil.getData(weburl);
        JSONObject object;
        if (result != null) {
            try {
                object = new JSONObject(result);
                String resultCode = object.getString("code");
                if (resultCode.equals("204")) {          //获取蓝牙设备坐标成功
                    JSONArray datas = object.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject data = (JSONObject) datas.get(i);
                        String id = data.getString("id");
                        String isBase = data.getString("is_bs");
                        String type = data.getString("type");
                        String info = null;
                        if (type.equals("1")) {        //一般
                            info = "良好";
                        } else if (type.equals("2")) {     //报警
                            info = "报警";
                        }
                        String mac = data.getString("mac");
                        String name = data.getString("name");
                        String x = data.getString("x");
                        String y = data.getString("y");
                        String time = data.getString("time");
                        BluetoothdeviceBean mBluetoothdevice = new BluetoothdeviceBean(id, name, isBase, info, mac, x, y, time);
                        if (mBluetoothdevice.getIsBase().equals("0")) {
                            mBluetoothdeviceBeanList.add(mBluetoothdevice);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从服务器获取手环地图的图片地址
     */
    private URL getMapUrl() {
        String result = HttpUtil.getData(ShowMapActivity.mapWeb);
        JSONObject object;
        URL mapUrl = null;
        if (result != null) {
            try {
                object = new JSONObject(result);
                /**
                 * 在你获取的string这个JSON对象中，提取你所需要的信息。
                 */
                String resultCode = object.getString("code");
                String map = object.getString("data");
                if (resultCode.equals("306")) {                 //获取手环地图成功
                    mapUrl = new URL(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mapUrl;
    }

    /**
     * 通过手环地图的图片地址获取Bitmap
     */
    private Bitmap getMapPic(URL mapUrl) {
        Bitmap bitmap = null;
        if (mapUrl != null) {
            try {
                HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 将图片存储到本地
     */
    private synchronized void savePicToSDcard(Bitmap bmp) {
        FileOutputStream fos = null;
        try {
            File div = new File(Environment.getExternalStorageDirectory(),
                    "ipcamera/map");
            if (!div.exists()) {
                div.mkdirs();
            }
            File file = new File(div, "map" + ".jpg");
            fos = new FileOutputStream(file);
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {         //bmp图片压缩成jpg
                fos.flush();
            }
        } catch (Exception e) {
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

}
