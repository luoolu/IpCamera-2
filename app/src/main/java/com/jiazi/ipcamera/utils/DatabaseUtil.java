package com.jiazi.ipcamera.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 存储摄像头的图片跟视频信息的数据库
 */

public class DatabaseUtil {

    /**
     * Database Name
     */
    private static final String DATABASE_NAME = "cameras.db";

    /**
     * Database Version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Table Name
     */
    private static final String DATABASW_VIDEOPICTURE_TABLE = "cameravidpic";
    private static final String DATABASE_ALARMLOG_TABLE = "alarmlog";
    private static final String DATABASE_CAMERA_TABLE = "camera";
    /**
     * Table columns
     */
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER = "user";
    public static final String KEY_PWD = "pwd";
    public static final String KEY_DID = "did";

    public static final String KEY_FILEPATH = "filepath";
    public static final String KEY_CREATETIME = "createtime";
    public static final String KEY_TYPE = "type";
    public static final String KEY_POSITION = "position";


    public static final String KEY_ALARMLOG_CONTENT = "content";

    /**
     * save video or picture to video_picture_table type
     */
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_PICTURE = "picture";

    /**
     * create alarmlog_table sql statement
     **/
    private static final String CREATE_ALARMLOG_TABLE =
            "create table " + DATABASE_ALARMLOG_TABLE + "("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_DID + " text not null, "
                    + KEY_ALARMLOG_CONTENT + " text not null, "
                    + KEY_CREATETIME + " text not null);";
    /**
     * create video_picture_table sql statement
     **/
    private static final String CREATE_VIDEO_PICTURE_TABLE =
            "create table " + DATABASW_VIDEOPICTURE_TABLE + "("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_DID + " text not null, "
                    + KEY_FILEPATH + " text not null, "
                    + KEY_CREATETIME + " text not null, "
                    + KEY_TYPE + " text not null);";


    /**
     * Context
     */
    private final Context mCtx;

    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Inner private class. Database Helper class for creating and updating database.
     */
    public static class DBHelper extends SQLiteOpenHelper {

        private static DBHelper mDBHelper = null;

        public static DBHelper getInstance(Context context) {
            if (mDBHelper == null) {
                mDBHelper = new DBHelper(context);
            }
            return mDBHelper;
        }

        // 数据库的构造方法，用来定义数据库的名称、数据库查询的结果集、数据库的版本
        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * 数据库第一次被创建时调用的方法
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建摄像头表
            db.execSQL("create table " + DATABASE_CAMERA_TABLE
                    + " (id integer primary key autoincrement, nickname varchar(20),"
                    + " did varchar(20), name varchar(20), password varchar(20), alarminfo varchar(20),"
                    + " mac varchar(20), xStart varchar(20), xStop varchar(20), yStart varchar(20), yStop varchar(20))");
            db.execSQL(CREATE_VIDEO_PICTURE_TABLE);
            db.execSQL(CREATE_ALARMLOG_TABLE);
        }

        // 当数据库的版本发生变化的时候（增加的时候）调用
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public DatabaseUtil(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * This method is used for creating/opening connection
     *
     * @return instance of DatabaseUtil
     * @throws SQLException
     */
    public DatabaseUtil open() throws SQLException {
        mDbHelper = DBHelper.getInstance(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * This method is used for closing the connection.
     */
    public void close() {
        mDbHelper.close();
    }


    /**
     * This Method is used to create/insert new record
     **/
    public long createVideoOrPic(String did, String filepath, String type, String createtime) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DID, did);
        initialValues.put(KEY_FILEPATH, filepath);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_CREATETIME, createtime);
        return mDb.insert(DATABASW_VIDEOPICTURE_TABLE, null, initialValues);
    }

    /**
     * This Method is used to query all video record from video_picture_table
     **/
    public Cursor queryAllVideo(String did) {
        String sql = "select * from " + DATABASW_VIDEOPICTURE_TABLE + " where  " + KEY_TYPE + "='" + TYPE_VIDEO + "' and " + KEY_DID + "='" + did + "' order by " + KEY_FILEPATH + " desc";
        return mDb.rawQuery(sql, null);
    }

    /**
     * This Method is used to query all picture record from video_picture_table
     **/
    public Cursor queryAllPicture(String did) {
        String sql = "select * from " + DATABASW_VIDEOPICTURE_TABLE + " where  " + KEY_TYPE + "='" + TYPE_PICTURE + "' and " + KEY_DID + "='" + did + "'";
        return mDb.rawQuery(sql, null);
    }

    /**
     * This Method is used to query video/picture in createtime from video_picture_table
     **/
    public Cursor queryVideoOrPictureByDate(String did, String date, String type) {
        String sql = "select * from " + DATABASW_VIDEOPICTURE_TABLE + " where  " + KEY_TYPE + "='" + TYPE_PICTURE + "' and " + KEY_DID + "='" + did + "' and " + KEY_CREATETIME + "='" + date + "'";
        return mDb.rawQuery(sql, null);
    }

    /***
     * This Method is used to delete specific video/picture record from video_picture_table
     */
    public boolean deleteVideoOrPicture(String did, String filePath, String type) {
        return mDb.delete(DATABASW_VIDEOPICTURE_TABLE, KEY_DID + "=? and " + KEY_FILEPATH + "=? and " + KEY_TYPE + "=?", new String[]{did, filePath, type}) > 0;
    }

    /**
     * This Method is used to delete all video/picture record from video_picture_table
     **/
    public boolean deleteAllVideoOrPicture(String did, String type) {
        return mDb.delete(DATABASW_VIDEOPICTURE_TABLE, KEY_DID + "=? and " + KEY_TYPE + "=?", new String[]{did, type}) > 0;
    }

    /**
     * This Method is used to delete all record from video_picture_table
     **/
    public boolean deldteAllVideoPicture(String did) {
        return mDb.delete(DATABASW_VIDEOPICTURE_TABLE, KEY_DID + "=?", new String[]{did}) > 0;
    }

    /**
     * This Method is used to add alarm log to alarmlog_table
     **/
    public long insertAlarmLogToDB(String did, String content, String createTime) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DID, did);
        initialValues.put(KEY_ALARMLOG_CONTENT, content);
        initialValues.put(KEY_CREATETIME, createTime);
        return mDb.insertOrThrow(DATABASE_ALARMLOG_TABLE, null, initialValues);
    }

    /**
     * This Method is used to query all alarmlog from the specified  did
     **/
    public Cursor queryAllAlarmLog(String did) {
        String sql = "select * from " + DATABASE_ALARMLOG_TABLE + " where " + KEY_DID + "='" + did + "' order by " + KEY_CREATETIME + " desc";
        return mDb.rawQuery(sql, null);
    }

    /**
     * This Method is used to delete one alarmlog with specified time from did
     **/
    public boolean delAlarmLog(String did, String createtime) {
        return mDb.delete(DATABASE_ALARMLOG_TABLE, KEY_DID + "=? and " + KEY_CREATETIME + "=?", new String[]{did, createtime}) > 0;
    }
}