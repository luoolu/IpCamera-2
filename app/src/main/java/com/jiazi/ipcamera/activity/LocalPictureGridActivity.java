package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiazi.ipcamera.adapter.ShowLocPicGridViewAdapter;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.utils.ContentCommon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *展示本地图片列表
 */
public class LocalPictureGridActivity extends Activity implements
        OnItemClickListener, OnClickListener {
    private String strDID;
    private String strDate;
    private ArrayList<String> aList;
    private ArrayList<Map<String, Object>> arrayList;
    private ImageButton btnBack;
    private TextView tvTakePicTime;
    private TextView tvSelectSum;
    private GridView gridView;
    private String strCameraName;
    private LinearLayout layoutDel;
    private TextView tvNoVideo;
    private List<File> files;
    private File file;
    private boolean isEditing = false;
    private int position = -1;
    private int seletNum;
    private Context mContext;
    private ShowLocPicGridViewAdapter mAdapter;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDataFromOther();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showlocalpicgrid);
        mContext = this;
        files = new ArrayList<File>();
        findView();
        setListener();
        tvTakePicTime.setText(strDate);
        mAdapter = new ShowLocPicGridViewAdapter(this, strDID);
        mAdapter.setMode(2);//
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initBmp();
    }

    private void initBmp() {
        new Thread() {
            public void run() {
                mAdapter.clearAll();
                for (int i = 0; i < arrayList.size(); i++) {
                    Map<String, Object> map = arrayList.get(i);
                    String path = (String) map.get("path");
                    file = new File(path);
                    files.add(file);
                    try {
                        Bitmap bitmap = getBitmapByWidth(path, dip2px(mContext, 80), 0);
                        if (bitmap != null) {
                            mAdapter.addBitmap(bitmap, path, 0);
                            handler.sendEmptyMessage(1);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (arrayList.size() == 0) {
            finish();
        }
    }

    private void setListener() {
        btnBack.setOnClickListener(this);

    }

    private void findView() {
        btnBack = (ImageButton) findViewById(R.id.back);
        tvTakePicTime = (TextView) findViewById(R.id.tv_time);
        tvSelectSum = (TextView) findViewById(R.id.tv_select_sum);
        gridView = (GridView) findViewById(R.id.gridView1);
        layoutDel = (LinearLayout) findViewById(R.id.del_bottom_layout);
        tvNoVideo = (TextView) findViewById(R.id.localpic_tv_nopic);
        tvNoVideo.setText(getResources().getString(R.string.no_video));
    }

    private void getDataFromOther() {
        Intent intent = getIntent();
        strDID = intent.getStringExtra("did");
        strDate = intent.getStringExtra("date");
        strCameraName = intent.getStringExtra(ContentCommon.STR_CAMERA_NAME);
        aList = (ArrayList<String>) intent.getSerializableExtra("list");
        arrayList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < aList.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            String path = aList.get(i);
            map.put("path", path);
            map.put("status", 0);
            arrayList.add(map);
        }
        aList.clear();
        aList = null;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        Intent intent = new Intent(Intent.ACTION_VIEW);        //调用外部相册查看图片
        intent.setDataAndType(Uri.fromFile(files.get(position)), "image/*");
        startActivity(intent);
        finish();
    }


    /**
     * 根据宽度从本地图片路径获取该图片的缩略图
     *
     * @param localImagePath 本地图片的路径
     * @param width          缩略图的宽
     * @param addedScaling   额外可以加的缩放比例
     * @return bitmap 指定宽高的缩略图
     */
    public static Bitmap getBitmapByWidth(String localImagePath, int width,
                                          int addedScaling) {
        if (TextUtils.isEmpty(localImagePath)) {
            return null;
        }

        Bitmap temBitmap = null;

        try {
            BitmapFactory.Options outOptions = new BitmapFactory.Options();

            // 设置该属性为true，不加载图片到内存，只返回图片的宽高到options中。
            outOptions.inJustDecodeBounds = true;

            // 加载获取图片的宽高
            BitmapFactory.decodeFile(localImagePath, outOptions);

            int height = outOptions.outHeight;

            if (outOptions.outWidth > width) {
                // 根据宽设置缩放比例
                outOptions.inSampleSize = outOptions.outWidth / width + 1
                        + addedScaling;
                outOptions.outWidth = width;

                // 计算缩放后的高度
                height = outOptions.outHeight / outOptions.inSampleSize;
                outOptions.outHeight = height;
            }

            // 重新设置该属性为false，加载图片返回
            outOptions.inJustDecodeBounds = false;
            outOptions.inPurgeable = true;
            outOptions.inInputShareable = true;
            temBitmap = BitmapFactory.decodeFile(localImagePath, outOptions);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return temBitmap;
    }

    /**
     * dp转换为px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.back) {
            if (isEditing) {
                seletNum = 0;
                tvSelectSum.setVisibility(View.GONE);
                isEditing = false;
                layoutDel.setVisibility(View.GONE);
                ArrayList<Map<String, Object>> arrayPics = mAdapter
                        .getArrayPics();
                for (int i = 0; i < arrayPics.size(); i++) {
                    Map<String, Object> map = arrayPics.get(i);
                    map.put("status", 0);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                finish();
            }

        } else {
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arrayList.clear();
        arrayList = null;
    }
}