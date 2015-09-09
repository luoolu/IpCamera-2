package vstc2.nativecaller;


import android.content.Context;

public class NativeCaller {
	static {
		System.loadLibrary("ffmpeg");
		System.loadLibrary("vstc2_jni");
		System.loadLibrary("avi_utils");
	}

	public native static int RecordLocal(String uid,int bRecordLocal);   //用于开始录像以及结束录像
	
	public native static int TransferMessage(String did, String msg, int len);     //透传CGI指令,用于联动部分
	
	public native static void UpgradeFirmware(String did,String servPath,String filePath, int type);   //更新固件
	
	public native static void StartSearch();         //开始搜索

	public native static void StopSearch();          //停止搜索
	
	public native static void Init();                //初始化视频解码器

	public native static void Free();               //释放PPPP连接
	
	public native static void FormatSD(String did);          //格式化SD卡
	
	public native static int StartPPPP(String did, String user, String pwd, int bEnableLanSearch,String accountname);  //开启PPPP连接

	public native static int StopPPPP(String did);          //断开PPPP连接

	public native static int StopPPPPLivestream(String did);         //关闭视频流

	public native static int StartPPPPLivestream(String did, int streamid,int substreamid);         //开启视频流

	public native static int PPPPPTZControl(String did, int command);          //摄像头上下左右移动

	/*
	 * @param 0 分辨率 1 亮度 2对比度
	 */
	public native static int PPPPCameraControl(String did, int param, int value);          //摄像头的参数设置（亮度、对比度、分辨率）

	public native static int PPPPGetCGI(String did, int cgi);           //获取CGI指令？

	public native static int PPPPStartAudio(String did);              //开始监听

	public native static int PPPPStopAudio(String did);              //停止监听

	public native static int PPPPStartTalk(String did);                //开始讲话

	public native static int PPPPStopTalk(String did);                //停止讲话

	public native static int PPPPTalkAudioData(String did, byte[] data, int len);      //发送语音数据

	public native static int PPPPNetworkDetect();                          //探测网络

	public native static void PPPPInitial(String svr);                  //初始化服务器
	
	public native static void PPPPInitialOther(String svr);             //初始化服务器？

	public native static int PPPPSetCallbackContext(Context object);            //初始化回调函数环境

	public native static int PPPPRebootDevice(String did);                   //设备重启

	public native static int PPPPRestorFactory(String did);                    //恢复出厂设置

	public native static int StartPlayBack(String did, String filename,               //开始回放
			int offset);

	public native static int StopPlayBack(String did);                         //停止回放

	public native static int PPPPGetSDCardRecordFileList(String did,            //获取录像文件列表
			int PageIndex, int PageSize);

	public native static int PPPPWifiSetting(String did, int enable,               //设置设备wifi参数
			String ssid, int channel, int mode, int authtype, int encryp,
			int keyformat, int defkey, String key1, String key2, String key3,
			String key4, int key1_bits, int key2_bits, int key3_bits,
			int key4_bits, String wpa_psk);

	public native static int PPPPNetworkSetting(String did, String ipaddr,          //设置设备网络参数
			String netmask, String gateway, String dns1, String dns2, int dhcp,
			int port, int rtsport);

	public native static int PPPPUserSetting(String did, String user1,              //设置用户参数
			String pwd1, String user2, String pwd2, String user3, String pwd3);

	public native static int PPPPDatetimeSetting(String did, int now, int tz,        //设置设备日期参数
			int ntp_enable, String ntp_svr);

	public native static int PPPPDDNSSetting(String did, int service,             //设置设备的DDNS参数
			String user, String pwd, String host, String proxy_svr,
			int ddns_mode, int proxy_port);

	public native static int PPPPMailSetting(String did, String svr, int port,        //邮箱设置
			String user, String pwd, int ssl, String sender, String receiver1,
			String receiver2, String receiver3, String receiver4);

	public native static int PPPPFtpSetting(String did, String svr_ftp,               //FTP设置
			String user, String pwd, String dir, int port, int mode,
			int upload_interval);

	public native static int PPPPPTZSetting(String did, int led_mod,              //摄像头云台参数设置
			int ptz_center_onstart, int ptz_run_times, int ptz_patrol_rate,
			int ptz_patrul_up_rate, int ptz_patrol_down_rate,
			int ptz_patrol_left_rate, int ptz_patrol_right_rate,
			int disable_preset);

	public native static int PPPPAlarmSetting(String did, int alarm_audio,int motion_armed,    //报警设置
			int motion_sensitivity, int input_armed, int ioin_level,
			int iolinkage, int ioout_level, int alarmpresetsit, int mail,
			int snapshot, int record, int upload_interval, int schedule_enable,
			int schedule_sun_0, int schedule_sun_1, int schedule_sun_2,
			int schedule_mon_0, int schedule_mon_1, int schedule_mon_2,
			int schedule_tue_0, int schedule_tue_1, int schedule_tue_2,
			int schedule_wed_0, int schedule_wed_1, int schedule_wed_2,
			int schedule_thu_0, int schedule_thu_1, int schedule_thu_2,
			int schedule_fri_0, int schedule_fri_1, int schedule_fri_2,
			int schedule_sat_0, int schedule_sat_1, int schedule_sat_2);

	public native static int PPPPSDRecordSetting(String did,                  //设置录像
			int record_cover_enable, int record_timer, int record_size,
			int record_time_enable, int record_schedule_sun_0,
			int record_schedule_sun_1, int record_schedule_sun_2,
			int record_schedule_mon_0, int record_schedule_mon_1,
			int record_schedule_mon_2, int record_schedule_tue_0,
			int record_schedule_tue_1, int record_schedule_tue_2,
			int record_schedule_wed_0, int record_schedule_wed_1,
			int record_schedule_wed_2, int record_schedule_thu_0,
			int record_schedule_thu_1, int record_schedule_thu_2,
			int record_schedule_fri_0, int record_schedule_fri_1,
			int record_schedule_fri_2, int record_schedule_sat_0,
			int record_schedule_sat_1, int record_schedule_sat_2);

	public native static int PPPPGetSystemParams(String did, int paramType);         //获取设置参数

	public native static int YUV4202RGB565(byte[] yuv, byte[] rgb, int width,        //YUV420P转RGB565
			int height);

	public native static int DecodeH264Frame(byte[] h264frame, int bIFrame,         //解析H264帧
			byte[] yuvbuf, int length, int[] size);

	/**
	 * H264转avi格式
	 */
	public native static int OpenAvi(String filename, String forcc, int height,
			int width, int framerate);

	public native static int CloseAvi();

	public native static int WriteData(byte[] data, int len, int keyframe);
}