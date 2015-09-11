package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jiazi.ipcamera.adapter.LocalPictureAndVideoAdapter;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.DatabaseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 展示本地图片以及视频的列表
 */
public class LocalPictureAndVideoActivity extends Activity implements
        OnClickListener, OnItemClickListener {
    private String TAG = "LocalPictureAndVideoActivity";
    private int wh;
    private String strDID;
    private String cameraName;
    private DatabaseUtil mDbUtil;
    private ArrayList<Map<String, ArrayList<String>>> ListItem;
    private List<String> videotimes;
    private ListView mListView;
    private TextView tvCameraName;
    private TextView tvEmpty;
    private ImageButton back;
    private LocalPictureAndVideoAdapter mAdapter;
    private ArrayList<MyItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDataFromOther();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        wh = width > height ? height : width;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_picture);
        mDbUtil = new DatabaseUtil(this);
        ListItem = new ArrayList<Map<String, ArrayList<String>>>();
        videotimes = new ArrayList<String>();
        findView();

    }

    @Override
    protected void onResume() {
        items = initData();
        sort(items);
        mAdapter = new LocalPictureAndVideoAdapter(this, items, wh / 5);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        if (items.isEmpty()) {
            showEmptyView();
        }
        super.onResume();
    }

    private void showEmptyView() {
        tvEmpty.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }


    public void sort(ArrayList<MyItem> items) {
        MyItem temps;
        MyItem pre;
        MyItem after;
        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < items.size() - 1; j++) {
                pre = items.get(j);
                after = items.get(j + 1);

                if (pre.data.compareTo(after.data) < 0)
                {
                    temps = items.get(j);
                    items.set(j, items.get(j + 1));
                    items.set(j + 1, temps);
                }
            }

        }
    }

    public ArrayList<MyItem> initData() {
        ArrayList<MyItem> items = new ArrayList<MyItem>();
        ListItem.clear();
        ListItem.add(initPicData());//从数据库加载图片跟视频文件到ListItem里
        ListItem.add(initVideoData());//

        for (int i = 0; i < ListItem.size(); i++) {
            Map<String, ArrayList<String>> map = ListItem.get(i);
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                MyItem item = new MyItem();
                String data = it.next();
                item.data = data;
                item.paths = map.get(data);
                item.type = item.paths.get(0).endsWith("jpg") ? 1 : 2;
                items.add(item);
            }

        }
        return items;
    }

    public class MyItem {
        public String data;
        public ArrayList<String> paths;
        public int type = -1;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void findView() {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(cameraName);
        tvEmpty = (TextView) findViewById(R.id.tv_empty);
        mListView = (ListView) findViewById(R.id.localpic_listview);
        tvCameraName = (TextView) findViewById(R.id.tv_camera_setting);
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    private Map<String, ArrayList<String>> initVideoData() {

        Map<String, ArrayList<String>> childMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> groupList = new ArrayList<String>();
        mDbUtil.open();
        Cursor cursor = mDbUtil.queryAllVideo(strDID);//查询
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor
                    .getColumnIndex(DatabaseUtil.KEY_FILEPATH));//根据列的下标
            File file = null;
            try {
                file = new File(filePath);
                if (file == null || !file.exists()) {
                    boolean delResult = mDbUtil.deleteVideoOrPicture(strDID,
                            filePath, DatabaseUtil.TYPE_VIDEO);
                    Log.d(TAG, "delResult:" + delResult);
                    continue;
                }
            } catch (Exception e) {

            }
            String s1 = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (!videotimes.contains(s1)) {
                videotimes.add(s1);
            }
            String date = s1.substring(0, 10);
            if (!groupList.contains(date)) {
                groupList.add(date);
                ArrayList<String> list = new ArrayList<String>();
                list.add(filePath);
                childMap.put(date, list);
            } else {
                childMap.get(date).add(filePath);
            }
        }
        mDbUtil.close();
        Collections.sort(groupList, new Comparator<String>() {

            @Override
            public int compare(String object1, String object2) {
                return object2.compareTo(object1);
            }
        });
        return childMap;
    }

    private Map<String, ArrayList<String>> initPicData() {
        Map<String, ArrayList<String>> childMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> groupList = new ArrayList<String>();
        mDbUtil.open();
        Cursor cursor = mDbUtil.queryAllPicture(strDID);
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor
                    .getColumnIndex(DatabaseUtil.KEY_FILEPATH));
            File file = null;
            try {
                file = new File(filePath);
                if (file == null || !file.exists()) {
                    boolean delResult = mDbUtil.deleteVideoOrPicture(strDID,
                            filePath, DatabaseUtil.TYPE_PICTURE);
                    Log.d(TAG, "delResult:" + delResult);
                    continue;// �������
                }
            } catch (Exception e) {

            }
            String s1 = filePath.substring(filePath.lastIndexOf("/") + 1);
            String date = s1.substring(0, 10);
            if (!groupList.contains(date)) {
                groupList.add(date);
                ArrayList<String> list = new ArrayList<String>();
                list.add(filePath);
                childMap.put(date, list);
                Log.i("info", "groupList:" + groupList);
                Log.i("info", "childMap:" + childMap);
            } else {
                childMap.get(date).add(filePath);
            }
        }

        mDbUtil.close();
        Collections.sort(groupList, new Comparator<String>() {

            @Override
            public int compare(String object1, String object2) {
                return object2.compareTo(object1);
            }
        });
        return childMap;

    }

    @Override
    public void onClick(View arg0) {
        finish();
        overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getDataFromOther() {
        Intent intent = getIntent();
        strDID = intent.getStringExtra(ContentCommon.STR_CAMERA_ID);
        cameraName = intent.getStringExtra(ContentCommon.STR_CAMERA_NAME);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {

        String date = items.get(position).data;
        Intent intent = null;
        int type = items.get(position).type;

        if (type == 1) {     //type为1时 代表为图片
            intent = new Intent(this, LocalPictureGridActivity.class);
        } else if (type == 2) {        //type为2时 代表为视频
            intent = new Intent(this, LocalVideoGridActivity.class);
            final ArrayList<String> arrayListtime = new ArrayList<String>();
            if (videotimes.size() > 0) {
                for (int i = 0; i < videotimes.size(); i++) {
                    String mess = videotimes.get(i).substring(0, 10);
                    if (mess.equals(date)) {
                        arrayListtime.add(videotimes.get(i));
                    }
                }
            }
            intent.putExtra("videotime", arrayListtime);
        }

        intent.putExtra("did", strDID);
        intent.putExtra("list", items.get(position).paths);
        intent.putExtra("date", date);
        intent.putExtra(ContentCommon.STR_CAMERA_NAME, cameraName);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // groupList.clear();
        // groupList = null;
        // childMap.clear();
        // childMap = null;
    }
}