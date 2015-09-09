package com.jiazi.ipcamera.utils;

/**
 * 常量类
 */
public class Msg {

	public static String user_id;// SharedPreferences配置文件的名称

	public static int[] device_on_images = null;//
	public static int[] device_off_images = null;//
	public static int[] qingjing_on_images = null;//
	public static int[] qingjing_off_images = null;//
	public static String[] TYPES = null;//

	public static final String CONFIG = "config";// SharedPreferences配置文件的名称

	public static final String DATABASE = "cameras.db";// 本地数据库的名称
	public static final String TABLE_USER = "user";// 用户表的名称
	public static final String TABLE_CAMERA = "camera";//摄像头表的名称

	public static final int STATE_OPEN = 1;// 设备状态开
	public static final int STATE_CLOSE = 0;// 设备状态关

	public static final int CONNECT_SUCCESS = 1;
	public static final int CONNECT_FAIL = 2;

}
