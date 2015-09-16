package com.jiazi.ipcamera.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jiazi.ipcamera.bean.CameraBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理CamaraBean以及数据库的实体类
 */
public class CameraManager {
    private String table_name = "camera";     //表名

    private static CameraManager mCameraManager = null;
    private static DatabaseUtil.DBHelper helper = null;
    private SQLiteDatabase db = null;

    public static CameraManager getInstance(Context context) {
        // 在DBHelper的构造方法里面完成helper的初始化
        if (mCameraManager == null) {
            mCameraManager = new CameraManager();
        }
        if (helper == null) {
            helper = DatabaseUtil.DBHelper.getInstance(context);
        }

        return mCameraManager;
    }

    public long addCamera(CameraBean camera) {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nickname", camera.getNickname());
        values.put("did", camera.getDid());
        values.put("name", camera.getName());
        values.put("password", camera.getPsw());
        values.put("alarminfo", camera.getAlarminfo());
        values.put("mac", camera.getMac());
        values.put("xStart", camera.getxStart());
        values.put("xStop", camera.getxStop());
        values.put("yStart", camera.getyStart());
        values.put("yStop", camera.getyStop());
        long id = db.insert(table_name, null, values);// 返回值是当前记录的id号，-1表示插入出错
        mDatabaseManager.closeDatabase();
        return id;
    }

    /**
     * 查询记录是否存在
     */
    public boolean isExist(String camera_did) {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();
        Cursor cursor = db.query(table_name, null, "did=?", new String[]{camera_did},
                null, null, null);
        boolean result = cursor.moveToNext();
        cursor.close();
        mDatabaseManager.closeDatabase();
        return result;
    }

    /**
     * 获得摄像头的数目
     */
    public List<CameraBean> getCameras() {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();
        List<CameraBean> cameraList = new ArrayList<CameraBean>();

        Cursor cursor = db.query(table_name, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String uid = cursor.getString(cursor.getColumnIndex("did"));
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            String alarminfo = cursor.getString(cursor.getColumnIndex("alarminfo"));
            String mac = cursor.getString(cursor.getColumnIndex("mac"));
            float xStart = cursor.getFloat(cursor.getColumnIndex("xStart"));
            float xStop = cursor.getFloat(cursor.getColumnIndex("xStop"));
            float yStart = cursor.getFloat(cursor.getColumnIndex("yStart"));
            float yStop = cursor.getFloat(cursor.getColumnIndex("yStop"));
            CameraBean camera = new CameraBean(uid, name, password, nickname, alarminfo, mac, xStart, xStop, yStart, yStop);

            cameraList.add(camera);
        }
        cursor.close();
        mDatabaseManager.closeDatabase();
        return cameraList;
    }

    /**
     * 删除一条记录
     */
    public int deleteCameraByUID(String uid) {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();
        int number = db.delete(table_name, "did=?", new String[]{uid});
        mDatabaseManager.closeDatabase();
        return number;
    }

    /**
     * 通过UID查找摄像头
     */
    public CameraBean getCameraByUID(String uid) {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();

        CameraBean camera = null;
        Cursor cursor = db.query(table_name, null, "did=?", new String[]{uid}, null,
                null, null);
        if (cursor.moveToNext()) {
            String did = cursor.getString(cursor.getColumnIndex("did"));
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String psw = cursor.getString(cursor.getColumnIndex("password"));
            String alarminfo = cursor.getString(cursor.getColumnIndex("alarminfo"));
            String mac = cursor.getString(cursor.getColumnIndex("mac"));
            float xStart = cursor.getFloat(cursor.getColumnIndex("xStart"));
            float xStop = cursor.getFloat(cursor.getColumnIndex("xStop"));
            float yStart = cursor.getFloat(cursor.getColumnIndex("yStart"));
            float yStop = cursor.getFloat(cursor.getColumnIndex("yStop"));
            camera = new CameraBean(did, name, psw, nickname, alarminfo, mac, xStart, xStop, yStart, yStop);
        }

        cursor.close();
        mDatabaseManager.closeDatabase();
        return camera;
    }

    /**
     * 修改一条记录
     */
    public int changeDevice(CameraBean camera) {
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(helper);
        db = mDatabaseManager.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("did", camera.getDid());
        values.put("name", camera.getName());
        values.put("password", camera.getPsw());
        values.put("nickname", camera.getNickname());
        values.put("alarminfo", camera.getAlarminfo());
        values.put("mac", camera.getMac());
        values.put("xStart", camera.getxStart());
        values.put("xStop", camera.getxStop());
        values.put("yStart", camera.getyStart());
        values.put("yStop", camera.getyStop());
        int number = db.update(table_name, values, "did=?", new String[]{camera.getDid()});
        mDatabaseManager.closeDatabase();
        return number;
    }
}
