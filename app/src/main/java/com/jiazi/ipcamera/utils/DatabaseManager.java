package com.jiazi.ipcamera.utils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用来解决多线程读取数据库并行性问题的帮助类
 *
 */
public class DatabaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();      //AtomicInteger，一个提供原子操作的Integer的类
    private static DatabaseManager mDatabaseManager;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;


    /**
     * 初始化数据库帮助类
     */
    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (mDatabaseManager == null) {
            mDatabaseManager = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    /**
     * 返回数据库帮助类
     */
    public static synchronized DatabaseManager getInstance(SQLiteOpenHelper helper) {
        if (mDatabaseManager == null) {
            initializeInstance(helper);
        }
        return mDatabaseManager;
    }

    /**
     * 获取数据库
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getReadableDatabase();
        }
        return mDatabase;
    }

    /**
     * 关闭数据库
     */
    public synchronized void closeDatabase() {

        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }
}
