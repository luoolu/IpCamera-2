package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.bean.AlarmBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.ContentCommon;

import vstc2.nativecaller.NativeCaller;

/**
 * 摄像头报警设置功能
 */
public class SettingAlarmActivity extends Activity implements
		OnClickListener, OnCheckedChangeListener, OnGestureListener,
		OnTouchListener, BridgeService.AlarmInterface {
	// private String TAG = "SettingAlermActivity";
	private String strDID = null;
	private boolean successFlag = false;
	private final int TIMEOUT = 3000;
	private final int ALERMPARAMS = 3;
	private final int UPLOADTIMETOOLONG = 4;
	private int cameraType = 0;
	private GestureDetector gt = new GestureDetector(this);
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				Snackbar.make(mCoordinatorLayout,getString(R.string.alerm_set_failed),Snackbar.LENGTH_SHORT).show();
				break;
			case 1:
				Snackbar.make(mCoordinatorLayout,getString(R.string.setting_aler_sucess),Snackbar.LENGTH_SHORT).show();
				finish();
				break;
			case ALERMPARAMS:
				successFlag = true;
				mMaterialDialog.cancel();
				if (0 == alarmBean.getMotion_armed()) {
					motionAlermView.setVisibility(View.GONE);

				} else {
					cbxMotionAlerm.setChecked(true);
					motionAlermView.setVisibility(View.VISIBLE);
				}
				tvSensitivity.setText(String.valueOf(alarmBean
						.getMotion_sensitivity()));

				if (0 == alarmBean.getInput_armed()) {

					ioAlermView.setVisibility(View.GONE);
				} else {
					cbxIOAlerm.setChecked(true);
					ioAlermView.setVisibility(View.VISIBLE);
				}

				if (0 == alarmBean.getIoin_level()) {
					tvTriggerLevel.setText(getResources().getString(
							R.string.alerm_ioin_levellow));
				} else {
					tvTriggerLevel.setText(getResources().getString(
							R.string.alerm_ioin_levelhight));
				}

				if (0 == alarmBean.getAlarm_audio()) {
					audioAlermView.setVisibility(View.GONE);
					audioSensitivity.setText(getResources().getString(
							R.string.alerm_audio_levelforbid));
				} else {
					cbxAudioAlerm.setChecked(true);
					audioAlermView.setVisibility(View.VISIBLE);
					if (1 == alarmBean.getAlarm_audio()) {
						audioSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levelhigh));
					} else if (2 == alarmBean.getAlarm_audio()) {
						audioSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levelmiddle));
					} else if (3 == alarmBean.getAlarm_audio()) {
						audioSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levellow));
					}
				}

				if (0 == alarmBean.getAlarm_temp()) {
					tempAlermView.setVisibility(View.GONE);
					tempSensitivity.setText(getResources().getString(
							R.string.alerm_audio_levelforbid));
				} else {
					cbxTempAlerm.setChecked(true);
					tempAlermView.setVisibility(View.VISIBLE);
					if (1 == alarmBean.getAlarm_temp()) {
						tempSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levelhigh));
					} else if (2 == alarmBean.getAlarm_temp()) {
						tempSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levelmiddle));
					} else if (3 == alarmBean.getAlarm_temp()) {
						tempSensitivity.setText(getResources().getString(
								R.string.alerm_audio_levellow));
					}
				}

				if (0 == alarmBean.getIolinkage()) {
					ioMotionView.setVisibility(View.GONE);
				} else {
					cbxIOMotion.setChecked(true);
					ioMotionView.setVisibility(View.VISIBLE);
				}

				if (0 == alarmBean.getIoout_level()) {
					tvIoOutLevel.setText(getResources().getString(
							R.string.alerm_ioin_levellow));
				} else {
					tvIoOutLevel.setText(getResources().getString(
							R.string.alerm_ioin_levelhight));
				}

				if (alarmBean.getAlermpresetsit() == 0) {
					tvPreset.setText(getResources().getString(
							R.string.alerm_preset_no));
				} else {
					tvPreset.setText(String.valueOf(alarmBean
							.getAlermpresetsit()));
				}

				if (1 == alarmBean.getMotion_armed()
						|| 1 == alarmBean.getInput_armed()
						|| alarmBean.getAlarm_audio() != 0) {
					eventView.setVisibility(View.VISIBLE);
				} else {
					eventView.setVisibility(View.GONE);
				}

				break;
			default:
				break;
			}
		}
	};

	private CoordinatorLayout mCoordinatorLayout;
	private ImageButton btnOk = null;
	private ImageButton btnCancel = null;
	private View motionAlermView = null;
	private View ioAlermView = null;
	private View audioAlermView = null;
	private View tempAlermView = null;
	private View ioMotionView = null;
	private CardView eventView = null;
	private LinearLayout alarm3518eOptionll = null;
	private ImageView imgTriggerLevelDrop = null;
	private ImageView audioImgDrop = null;
	private ImageView tempImgDrop = null;
	private ImageView imgSensitiveDrop = null;
	private ImageView imgPresetDrop = null;
	private ImageView imgIoOutLevelDrop = null;
	private TextView tvIoOutLevel = null;
	private TextView tvPreset = null;
	private TextView tvTriggerLevel = null;
	private TextView tvSensitivity = null;
	private TextView audioSensitivity = null;
	private TextView tempSensitivity = null;
	private CheckBox cbxIOMotion = null;
	private CheckBox cbxIOAlerm = null;
	private CheckBox cbxAudioAlerm = null;
	private CheckBox cbxTempAlerm = null;
	private CheckBox cbxMotionAlerm = null;
	private AlarmBean alarmBean = null;

	private PopupWindow sensitivePopWindow = null;
	private PopupWindow triggerLevelPopWindow = null;
	private PopupWindow ioOutLevelPopWindow = null;
	private PopupWindow presteMovePopWindow = null;
	private PopupWindow audioPopWindow = null;

	private MaterialDialog mMaterialDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		getDataFromOther();
		setContentView(R.layout.activity_settingalarm);
		NativeCaller.PPPPGetSystemParams(strDID,
				ContentCommon.MSG_TYPE_GET_PARAMS);

		MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
		mMaterialDialog = builder.content(getString(R.string.alerm_getparams))
				.progress(true,0)
				.progressIndeterminateStyle(false)
				.show();
		mHandler.postDelayed(runnable, TIMEOUT);
		alarmBean = new AlarmBean();
		findView();
		setListener();
		BridgeService.setAlarmInterface(this);

		initPopupWindow();
	}

	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);// �˳�����
		super.onPause();
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (!successFlag) {
				mMaterialDialog.dismiss();
			}
		}
	};

	private void setListener() {
		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		imgIoOutLevelDrop.setOnClickListener(this);
		imgPresetDrop.setOnClickListener(this);
		imgSensitiveDrop.setOnClickListener(this);
		imgTriggerLevelDrop.setOnClickListener(this);
		audioImgDrop.setOnClickListener(this);
		tempImgDrop.setOnClickListener(this);
		cbxMotionAlerm.setOnCheckedChangeListener(this);
		cbxIOAlerm.setOnCheckedChangeListener(this);
		cbxAudioAlerm.setOnCheckedChangeListener(this);
		cbxTempAlerm.setOnCheckedChangeListener(this);
		cbxIOMotion.setOnCheckedChangeListener(this);
		eventView.setOnTouchListener(this);
		ioMotionView.setOnTouchListener(this);
		scrollView.setOnTouchListener(this);
		mMaterialDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}

		});
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.alerm_ok) {
			setAlerm();

		} else if (i == R.id.alerm_cancel) {
			finish();

		} else if (i == R.id.alerm_img_ioout_level_drop) {
			dismissPopupWindow();

			ioOutLevelPopWindow.showAsDropDown(imgIoOutLevelDrop, -140, 0);

		} else if (i == R.id.ioout_hight) {
			tvIoOutLevel.setText(getResources().getString(
					R.string.alerm_ioin_levelhight));
			alarmBean.setIoout_level(1);

		} else if (i == R.id.ioout_low) {
			tvIoOutLevel.setText(getResources().getString(
					R.string.alerm_ioin_levellow));
			alarmBean.setIoout_level(0);

		} else if (i == R.id.alerm_img_preset_drop) {
			dismissPopupWindow();

			presteMovePopWindow.showAsDropDown(imgPresetDrop, -160, 0);


		} else if (i == R.id.alerm_img_sensitive_drop) {
			dismissPopupWindow();

			sensitivePopWindow.showAsDropDown(imgSensitiveDrop, -120, 10);

		} else if (i == R.id.alerm_img_leveldrop) {
			dismissPopupWindow();

			triggerLevelPopWindow.showAsDropDown(imgTriggerLevelDrop, -140, 0);

		} else if (i == R.id.alerm_audio_leveldrop) {
			isTempAlarm = false;
			dismissPopupWindow();

			audioPopWindow.showAsDropDown(audioImgDrop, -140, 0);


		} else if (i == R.id.alerm_temp_leveldrop) {
			isTempAlarm = true;
			dismissPopupWindow();

			audioPopWindow.showAsDropDown(tempImgDrop, -140, 0);

		} else if (i == R.id.trigger_audio_levelhigh) {
			if (isTempAlarm) {
				alarmBean.setAlarm_temp(1);
				tempSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelhigh));
			} else {
				audioSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelhigh));
				alarmBean.setAlarm_audio(1);
			}
			audioPopWindow.dismiss();

		} else if (i == R.id.trigger_audio_levelmiddle) {
			if (isTempAlarm) {
				alarmBean.setAlarm_temp(2);
				tempSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelmiddle));
			} else {
				audioSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelmiddle));
				alarmBean.setAlarm_audio(2);
			}
			audioPopWindow.dismiss();

		} else if (i == R.id.trigger_audio_levellow) {
			if (isTempAlarm) {
				alarmBean.setAlarm_temp(3);
				tempSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levellow));
			} else {

				audioSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levellow));
				alarmBean.setAlarm_audio(3);
			}
			audioPopWindow.dismiss();

		} else if (i == R.id.trigger_audio_levelforbid) {
			if (isTempAlarm) {
				alarmBean.setAlarm_temp(0);
				tempSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelforbid));
			} else {
				audioSensitivity.setText(getResources().getString(
						R.string.alerm_audio_levelforbid));
				alarmBean.setAlarm_audio(0);
			}
			audioPopWindow.dismiss();

		} else if (i == R.id.trigger_hight) {
			tvTriggerLevel.setText(getResources().getString(
					R.string.alerm_ioin_levelhight));
			triggerLevelPopWindow.dismiss();
			alarmBean.setIoin_level(1);

		} else if (i == R.id.trigger_low) {
			tvTriggerLevel.setText(getResources().getString(
					R.string.alerm_ioin_levellow));
			triggerLevelPopWindow.dismiss();
			alarmBean.setIoin_level(0);

		} else if (i == R.id.sensitive_10) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(10);
			tvSensitivity.setText(String.valueOf(10));

		} else if (i == R.id.sensitive_9) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(9);
			tvSensitivity.setText(String.valueOf(9));

		} else if (i == R.id.sensitive_8) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(8);
			tvSensitivity.setText(String.valueOf(8));

		} else if (i == R.id.sensitive_7) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(7);
			tvSensitivity.setText(String.valueOf(7));

		} else if (i == R.id.sensitive_6) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(6);
			tvSensitivity.setText(String.valueOf(6));

		} else if (i == R.id.sensitive_5) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(5);
			tvSensitivity.setText(String.valueOf(5));

		} else if (i == R.id.sensitive_4) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(4);
			tvSensitivity.setText(String.valueOf(4));

		} else if (i == R.id.sensitive_3) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(3);
			tvSensitivity.setText(String.valueOf(3));

		} else if (i == R.id.sensitive_2) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(2);
			tvSensitivity.setText(String.valueOf(2));

		} else if (i == R.id.sensitive_1) {
			sensitivePopWindow.dismiss();
			alarmBean.setMotion_sensitivity(1);
			tvSensitivity.setText(String.valueOf(1));

		} else if (i == R.id.preset_no) {
			alarmBean.setAlermpresetsit(0);
			tvPreset.setText(getResources().getString(R.string.alerm_preset_no));
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_1) {
			alarmBean.setAlermpresetsit(1);
			tvPreset.setText("1");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_2) {
			alarmBean.setAlermpresetsit(2);
			tvPreset.setText("2");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_3) {
			alarmBean.setAlermpresetsit(3);
			tvPreset.setText("3");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_4) {
			alarmBean.setAlermpresetsit(4);
			tvPreset.setText("4");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_5) {
			alarmBean.setAlermpresetsit(5);
			tvPreset.setText("5");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_6) {
			alarmBean.setAlermpresetsit(6);
			tvPreset.setText("6");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_7) {
			alarmBean.setAlermpresetsit(7);
			tvPreset.setText("7");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_8) {
			alarmBean.setAlermpresetsit(8);
			tvPreset.setText("8");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_9) {
			alarmBean.setAlermpresetsit(9);
			tvPreset.setText("9");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_10) {
			alarmBean.setAlermpresetsit(10);
			tvPreset.setText("10");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_11) {
			alarmBean.setAlermpresetsit(11);
			tvPreset.setText("11");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_12) {
			alarmBean.setAlermpresetsit(12);
			tvPreset.setText("12");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_13) {
			alarmBean.setAlermpresetsit(13);
			tvPreset.setText("13");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_14) {
			alarmBean.setAlermpresetsit(14);
			tvPreset.setText("14");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_15) {
			alarmBean.setAlermpresetsit(15);
			tvPreset.setText("15");
			presteMovePopWindow.dismiss();

		} else if (i == R.id.preset_16) {
			alarmBean.setAlermpresetsit(16);
			tvPreset.setText("16");
			presteMovePopWindow.dismiss();

		} else {
		}
	}

	private void setAlerm() {
		if (successFlag) {
			Log.e("setAlerm", "setAlermTemp: " + alarmBean.getAlarm_temp());
			NativeCaller.PPPPAlarmSetting(strDID, alarmBean.getAlarm_audio(),
					alarmBean.getMotion_armed(),
					alarmBean.getMotion_sensitivity(),
					alarmBean.getInput_armed(), alarmBean.getIoin_level(),
					alarmBean.getIoout_level(), alarmBean.getIolinkage(),
					alarmBean.getAlermpresetsit(), alarmBean.getMail(),
					alarmBean.getSnapshot(), alarmBean.getRecord(),
					alarmBean.getUpload_interval(),
					alarmBean.getSchedule_enable(),
					0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
					0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
					0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
					0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
					0xFFFFFFFF);
		} else {
			Snackbar.make(mCoordinatorLayout,getString(R.string.alerm_set_failed),Snackbar.LENGTH_SHORT).show();
		}
	}

	private ScrollView scrollView;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gt.onTouchEvent(event);
	}

	private void findView() {

		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_setting_alarm);
		cbxMotionAlerm = (CheckBox) findViewById(R.id.alerm_cbx_move_layout);
		cbxIOAlerm = (CheckBox) findViewById(R.id.alerm_cbx_i0_layout);
		cbxAudioAlerm = (CheckBox) findViewById(R.id.alerm_cbx_audio_layout);
		cbxTempAlerm = (CheckBox) findViewById(R.id.alerm_cbx_temp_layout);
		cbxIOMotion = (CheckBox) findViewById(R.id.alerm_cbx_io_move);

		tvSensitivity = (TextView) findViewById(R.id.alerm_tv_sensitivity);
		audioSensitivity = (TextView) findViewById(R.id.alerm_audio_triggerlevel);
		tempSensitivity = (TextView) findViewById(R.id.alerm_temp_triggerlevel);
		tvTriggerLevel = (TextView) findViewById(R.id.alerm_tv_triggerlevel);
		tvPreset = (TextView) findViewById(R.id.alerm_tv_preset);
		tvIoOutLevel = (TextView) findViewById(R.id.alerm_tv_ioout_level_value);

		imgIoOutLevelDrop = (ImageView) findViewById(R.id.alerm_img_ioout_level_drop);
		imgPresetDrop = (ImageView) findViewById(R.id.alerm_img_preset_drop);
		imgSensitiveDrop = (ImageView) findViewById(R.id.alerm_img_sensitive_drop);
		imgTriggerLevelDrop = (ImageView) findViewById(R.id.alerm_img_leveldrop);
		audioImgDrop = (ImageView) findViewById(R.id.alerm_audio_leveldrop);
		tempImgDrop = (ImageView) findViewById(R.id.alerm_temp_leveldrop);

		alarm3518eOptionll = (LinearLayout) findViewById(R.id.alarm_3518e_option_view);
		if (cameraType == 1) {
			alarm3518eOptionll.setVisibility(View.VISIBLE);
		}

		ioMotionView = findViewById(R.id.alerm_io_move_view);
		ioAlermView = findViewById(R.id.alerm_ioview);
		audioAlermView = findViewById(R.id.alerm_audio_level);
		tempAlermView = findViewById(R.id.alerm_temp_level);
		motionAlermView = findViewById(R.id.alerm_moveview);
		eventView = (CardView) findViewById(R.id.alerm_eventview);

		btnOk = (ImageButton) findViewById(R.id.alerm_ok);
		btnCancel = (ImageButton) findViewById(R.id.alerm_cancel);

		scrollView = (ScrollView) findViewById(R.id.scrollView1);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.top_bg);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		drawable.setDither(true);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int i = buttonView.getId();
		if (i == R.id.alerm_cbx_move_layout) {
			if (isChecked) {
				alarmBean.setMotion_armed(1);
				motionAlermView.setVisibility(View.VISIBLE);
			} else {
				alarmBean.setMotion_armed(0);
				motionAlermView.setVisibility(View.GONE);
			}


		} else if (i == R.id.alerm_cbx_i0_layout) {
			if (isChecked) {
				alarmBean.setInput_armed(1);
				ioAlermView.setVisibility(View.VISIBLE);
			} else {
				alarmBean.setInput_armed(0);
				ioAlermView.setVisibility(View.GONE);
			}

		} else if (i == R.id.alerm_cbx_audio_layout) {
			if (isChecked) {
				alarmBean.setAudioArmedCheck(1);
				audioAlermView.setVisibility(View.VISIBLE);
			} else {
				alarmBean.setAudioArmedCheck(0);
				audioAlermView.setVisibility(View.GONE);
			}

		} else if (i == R.id.alerm_cbx_temp_layout) {
			if (isChecked) {
				alarmBean.setAlarmTempChecked(1);
				tempAlermView.setVisibility(View.VISIBLE);
			} else {
				alarmBean.setAlarmTempChecked(0);
				tempAlermView.setVisibility(View.GONE);
			}

		} else if (i == R.id.alerm_cbx_io_move) {
			if (isChecked) {
				alarmBean.setIolinkage(1);
				ioMotionView.setVisibility(View.VISIBLE);
			} else {
				alarmBean.setIolinkage(0);
				ioMotionView.setVisibility(View.GONE);
			}

		}
		if (1 == alarmBean.getMotion_armed() || 1 == alarmBean.getInput_armed()
				|| alarmBean.getAudioArmedCheck() == 1
				|| alarmBean.getAlarmTempChecked() == 1) {
			eventView.setVisibility(View.VISIBLE);
		} else {
			eventView.setVisibility(View.GONE);
		}
	}

	private void getDataFromOther() {
		Intent intent = getIntent();
		strDID = intent.getStringExtra(ContentCommon.STR_CAMERA_ID);
		cameraType = intent.getIntExtra(ContentCommon.STR_CAMERA_TYPE, 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissPopupWindow();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		dismissPopupWindow();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void callBackAlarmParams(String did, int alarm_audio, int motion_armed,
			int motion_sensitivity, int input_armed, int ioin_level,
			int iolinkage, int ioout_level, int alarmpresetsit, int mail,
			int snapshot, int record, int upload_interval,
			int schedule_enable, int schedule_sun_0, int schedule_sun_1,
			int schedule_sun_2, int schedule_mon_0, int schedule_mon_1,
			int schedule_mon_2, int schedule_tue_0, int schedule_tue_1,
			int schedule_tue_2, int schedule_wed_0, int schedule_wed_1,
			int schedule_wed_2, int schedule_thu_0, int schedule_thu_1,
			int schedule_thu_2, int schedule_fri_0, int schedule_fri_1,
			int schedule_fri_2, int schedule_sat_0, int schedule_sat_1,
			int schedule_sat_2) {

		alarmBean.setDid(did);
		alarmBean.setMotion_armed(motion_armed);
		alarmBean.setMotion_sensitivity(motion_sensitivity);
		alarmBean.setInput_armed(input_armed);
		alarmBean.setIoin_level(ioin_level);
		alarmBean.setIolinkage(iolinkage);
		alarmBean.setIoout_level(ioout_level);
		alarmBean.setAlermpresetsit(alarmpresetsit);
		alarmBean.setMail(mail);
		alarmBean.setSnapshot(snapshot);
		alarmBean.setRecord(record);
		alarmBean.setUpload_interval(upload_interval);
		alarmBean.setAlarm_audio(alarm_audio);
		alarmBean.setAlarm_temp(input_armed);
		alarmBean.setSchedule_enable(schedule_enable);
		mHandler.sendEmptyMessage(ALERMPARAMS);
	}

	@Override
	public void callBackSetSystemParamsResult(String did, int paramType,
			int result) {
		mHandler.sendEmptyMessage(result);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dismissPopupWindow();
		return false;
	}

	private void dismissPopupWindow() {
		if (presteMovePopWindow != null && presteMovePopWindow.isShowing()) {
			presteMovePopWindow.dismiss();
		}
		if (sensitivePopWindow != null && sensitivePopWindow.isShowing()) {
			sensitivePopWindow.dismiss();
		}
		if (triggerLevelPopWindow != null && triggerLevelPopWindow.isShowing()) {
			triggerLevelPopWindow.dismiss();
		}
		if (ioOutLevelPopWindow != null && ioOutLevelPopWindow.isShowing()) {
			ioOutLevelPopWindow.dismiss();
		}
		if (audioPopWindow != null && audioPopWindow.isShowing()) {
			audioPopWindow.dismiss();
		}
	}

	private boolean isTempAlarm = false;

	private void initPopupWindow() {
		// TODO Auto-generated method stub
		initAudioPopupWindow();
		initMovePopupWindow();
		initInputPopupWindow();
		initIOlinkMovePopupWindow();
		initPresetPopupWindow();
	}

	private void initPresetPopupWindow() {
		LinearLayout preLayout = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.alarmpresetmovepopwindow, null);
		TextView presetNo = (TextView) preLayout.findViewById(R.id.preset_no);
		TextView preset1 = (TextView) preLayout.findViewById(R.id.preset_1);
		TextView preset2 = (TextView) preLayout.findViewById(R.id.preset_2);
		TextView preset3 = (TextView) preLayout.findViewById(R.id.preset_3);
		TextView preset4 = (TextView) preLayout.findViewById(R.id.preset_4);
		TextView preset5 = (TextView) preLayout.findViewById(R.id.preset_5);
		TextView preset6 = (TextView) preLayout.findViewById(R.id.preset_6);
		TextView preset7 = (TextView) preLayout.findViewById(R.id.preset_7);
		TextView preset8 = (TextView) preLayout.findViewById(R.id.preset_8);
		TextView preset9 = (TextView) preLayout.findViewById(R.id.preset_9);
		TextView preset10 = (TextView) preLayout.findViewById(R.id.preset_10);
		TextView preset11 = (TextView) preLayout.findViewById(R.id.preset_11);
		TextView preset12 = (TextView) preLayout.findViewById(R.id.preset_12);
		TextView preset13 = (TextView) preLayout.findViewById(R.id.preset_13);
		TextView preset14 = (TextView) preLayout.findViewById(R.id.preset_14);
		TextView preset15 = (TextView) preLayout.findViewById(R.id.preset_15);
		TextView preset16 = (TextView) preLayout.findViewById(R.id.preset_16);
		presetNo.setOnClickListener(this);
		preset1.setOnClickListener(this);
		preset2.setOnClickListener(this);
		preset3.setOnClickListener(this);
		preset4.setOnClickListener(this);
		preset5.setOnClickListener(this);
		preset6.setOnClickListener(this);
		preset7.setOnClickListener(this);
		preset8.setOnClickListener(this);
		preset9.setOnClickListener(this);
		preset10.setOnClickListener(this);
		preset11.setOnClickListener(this);
		preset12.setOnClickListener(this);
		preset13.setOnClickListener(this);
		preset14.setOnClickListener(this);
		preset15.setOnClickListener(this);
		preset16.setOnClickListener(this);
		presteMovePopWindow = new PopupWindow(preLayout, 160,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	private void initIOlinkMovePopupWindow() {
		LinearLayout outLayout = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.alarmiooutpopwindow, null);
		TextView outHight = (TextView) outLayout.findViewById(R.id.ioout_hight);
		TextView outLow = (TextView) outLayout.findViewById(R.id.ioout_low);
		outHight.setOnClickListener(this);
		outLow.setOnClickListener(this);
		ioOutLevelPopWindow = new PopupWindow(outLayout, 160,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	private void initMovePopupWindow() {

		LinearLayout layout1 = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.alarmsensitivepopwindow, null);
		TextView sensitive10 = (TextView) layout1
				.findViewById(R.id.sensitive_10);
		TextView sensitive9 = (TextView) layout1.findViewById(R.id.sensitive_9);
		TextView sensitive8 = (TextView) layout1.findViewById(R.id.sensitive_8);
		TextView sensitive7 = (TextView) layout1.findViewById(R.id.sensitive_7);
		TextView sensitive6 = (TextView) layout1.findViewById(R.id.sensitive_6);
		TextView sensitive5 = (TextView) layout1.findViewById(R.id.sensitive_5);
		TextView sensitive4 = (TextView) layout1.findViewById(R.id.sensitive_4);
		TextView sensitive3 = (TextView) layout1.findViewById(R.id.sensitive_3);
		TextView sensitive2 = (TextView) layout1.findViewById(R.id.sensitive_2);
		TextView sensitive1 = (TextView) layout1.findViewById(R.id.sensitive_1);
		sensitive10.setOnClickListener(this);
		sensitive9.setOnClickListener(this);
		sensitive8.setOnClickListener(this);
		sensitive7.setOnClickListener(this);
		sensitive6.setOnClickListener(this);
		sensitive5.setOnClickListener(this);
		sensitive4.setOnClickListener(this);
		sensitive3.setOnClickListener(this);
		sensitive2.setOnClickListener(this);
		sensitive1.setOnClickListener(this);
		sensitivePopWindow = new PopupWindow(layout1, 160,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	private void initInputPopupWindow() {
		LinearLayout triggerLayout = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.alarmtriggerpopwindow, null);
		TextView tvHight = (TextView) triggerLayout
				.findViewById(R.id.trigger_hight);
		TextView tvLow = (TextView) triggerLayout
				.findViewById(R.id.trigger_low);
		tvLow.setOnClickListener(this);
		tvHight.setOnClickListener(this);
		triggerLevelPopWindow = new PopupWindow(triggerLayout, 160,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	private void initAudioPopupWindow() {
		// TODO Auto-generated method stub
		LinearLayout audiotriggerLayout = (LinearLayout) LayoutInflater.from(
				this).inflate(R.layout.alarmaudiopopwindow, null);
		TextView senHight = (TextView) audiotriggerLayout
				.findViewById(R.id.trigger_audio_levelhigh);
		TextView senMiddle = (TextView) audiotriggerLayout
				.findViewById(R.id.trigger_audio_levelmiddle);
		TextView senLow = (TextView) audiotriggerLayout
				.findViewById(R.id.trigger_audio_levellow);
		TextView senForbid = (TextView) audiotriggerLayout
				.findViewById(R.id.trigger_audio_levelforbid);
		senHight.setOnClickListener(this);
		senLow.setOnClickListener(this);
		senMiddle.setOnClickListener(this);
		senForbid.setOnClickListener(this);
		audioPopWindow = new PopupWindow(audiotriggerLayout, 160,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

}
