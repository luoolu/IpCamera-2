package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.bean.SdcardBean;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.BridgeService;
import com.jiazi.ipcamera.utils.ContentCommon;

import vstc2.nativecaller.NativeCaller;

/**
 * 摄像头SD卡设置功能
 */
public class SettingSDCardActivity extends Activity implements
		OnClickListener, OnCheckedChangeListener, BridgeService.SDCardInterface {
	private CoordinatorLayout mCoordinatorLayout;
	private TextView tvSdTotal = null;
	private TextView tvSdRemain = null;
	private TextView tvSdStatus = null;
	private Button btnFormat = null;
	private CheckBox cbxConverage = null;
	private EditText editRecordLength = null;
	private CheckBox cbxRecordTime = null;
	private ImageButton btnBack = null;
	private ImageButton btnOk = null;
	private final int TIMEOUT = 3000;
	private String strDID = null;// camera id
	// private String cameraName = null;
	private MaterialDialog mMaterialDialog = null;
	private boolean successFlag = false;// 获取和设置的结果
	private final int FAILED = 0;
	private final int SUCCESS = 1;
	private final int PARAMS = 2;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FAILED:
				Snackbar.make(mCoordinatorLayout,getString(R.string.sdcard_set_failed),Snackbar.LENGTH_SHORT).show();
				break;
			case SUCCESS:
				Snackbar.make(mCoordinatorLayout,getString(R.string.sdcard_set_success),Snackbar.LENGTH_SHORT).show();
				finish();
				break;
			case PARAMS:
				successFlag = true;
				mMaterialDialog.dismiss();
				tvSdTotal.setText(sdcardBean.getSdtotal() + "MB");
				tvSdRemain.setText(sdcardBean.getSdfree() + "MB");
				if (sdcardBean.getRecord_sd_status() == 1)
				{
					tvSdStatus
							.setText(SettingSDCardActivity.this.getResources()
									.getString(R.string.sdcard_inserted));
				} 
				else if (sdcardBean.getRecord_sd_status() == 2)
				{
					tvSdStatus.setText(getString(R.string.sdcard_video));
				}
				else if(sdcardBean.getRecord_sd_status() == 3)
				{
					tvSdStatus.setText(getString(R.string.sdcard_file_error));
				}
				else if(sdcardBean.getRecord_sd_status() == 4)
				{
					tvSdStatus.setText(getString(R.string.sdcard_isformatting));
				}
				else {
					tvSdStatus.setText(SettingSDCardActivity.this
							.getResources().getString(
									R.string.sdcard_status_info));
				}
				cbxConverage.setChecked(true);
				if (sdcardBean.getRecord_time_enable() == 1) {
					cbxRecordTime.setChecked(true);
				} else {
					cbxRecordTime.setChecked(false);
				}
				// editRecordLength.setText(sdcardBean.getRecord_timer() + "");
				editRecordLength.setText(15 + "");
				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);// �?��动画
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDataFromOther();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settingsdcard);
		MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
		mMaterialDialog = builder.content(getString(R.string.sdcard_getparams))
				.progress(true,0)
				.progressIndeterminateStyle(false)
				.show();
		sdcardBean = new SdcardBean();
		handler.postDelayed(runnable, TIMEOUT);
		findView();
		setLister();
		BridgeService.setSDCardInterface(this);
		NativeCaller.PPPPGetSystemParams(strDID,
				ContentCommon.MSG_TYPE_GET_RECORD);
	}

	private void getDataFromOther() {
		Intent intent = getIntent();
		strDID = intent.getStringExtra(ContentCommon.STR_CAMERA_ID);
		Log.i("info", "did:" + strDID);
		// cameraName = intent.getStringExtra(ContentCommon.STR_CAMERA_NAME);
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (!successFlag) {
				successFlag = false;
				mMaterialDialog.dismiss();
			}
		}
	};
	private SdcardBean sdcardBean;

	private void setLister() {
		btnBack.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		btnFormat.setOnClickListener(this);
		cbxConverage.setOnCheckedChangeListener(this);
		cbxRecordTime.setOnCheckedChangeListener(this);

	}

	private void findView() {
		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_setting_sdcard);
		tvSdTotal = (TextView) findViewById(R.id.tv_sd_total);
		tvSdRemain = (TextView) findViewById(R.id.tv_sd_remain);
		tvSdStatus = (TextView) findViewById(R.id.tv_state);
		btnFormat = (Button) findViewById(R.id.btn_format);
		cbxConverage = (CheckBox) findViewById(R.id.cbx_coverage);
		editRecordLength = (EditText) findViewById(R.id.edit_record_length);
		cbxRecordTime = (CheckBox) findViewById(R.id.cbx_record_time);
		btnBack = (ImageButton) findViewById(R.id.back);
		btnOk = (ImageButton) findViewById(R.id.ok);
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.back) {
			finish();

		} else if (i == R.id.ok) {
			setSDCardSchedule();

		} else if (i == R.id.btn_format) {
			showFormatDialog();

		} else {
		}
	}

	void showFormatDialog() {

		MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
		builder.title("提示")
				.content(R.string.sdcard_formatsd)
				.positiveText(R.string.str_ok).positiveColor(getResources().getColor(R.color.colorPrimary))
				.negativeText(R.string.str_cancel).negativeColor(getResources().getColor(R.color.colorPrimary))
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						NativeCaller.FormatSD(strDID);
						dialog.dismiss();
					}

					@Override
					public void onNegative(MaterialDialog dialog) {
						super.onNegative(dialog);
						dialog.dismiss();
					}
				})
				.show();
	}


	private void setSDCardSchedule() {

		if (sdcardBean.getRecord_time_enable() == 0) {
			sdcardBean.setSun_0(0);
			sdcardBean.setSun_1(0);
			sdcardBean.setSun_2(0);
			sdcardBean.setMon_0(0);
			sdcardBean.setMon_1(0);
			sdcardBean.setMon_2(0);
			sdcardBean.setTue_0(0);
			sdcardBean.setTue_1(0);
			sdcardBean.setTue_2(0);
			sdcardBean.setWed_0(0);
			sdcardBean.setWed_1(0);
			sdcardBean.setWed_2(0);
			sdcardBean.setThu_0(0);
			sdcardBean.setThu_1(0);
			sdcardBean.setThu_2(0);
			sdcardBean.setFri_0(0);
			sdcardBean.setFri_1(0);
			sdcardBean.setFri_2(0);
			sdcardBean.setSat_0(0);
			sdcardBean.setSat_1(0);
			sdcardBean.setSat_2(0);
		} else {
			sdcardBean.setSun_0(-1);
			sdcardBean.setSun_1(-1);
			sdcardBean.setSun_2(-1);
			sdcardBean.setMon_0(-1);
			sdcardBean.setMon_1(-1);
			sdcardBean.setMon_2(-1);
			sdcardBean.setTue_0(-1);
			sdcardBean.setTue_1(-1);
			sdcardBean.setTue_2(-1);
			sdcardBean.setWed_0(-1);
			sdcardBean.setWed_1(-1);
			sdcardBean.setWed_2(-1);
			sdcardBean.setThu_0(-1);
			sdcardBean.setThu_1(-1);
			sdcardBean.setThu_2(-1);
			sdcardBean.setFri_0(-1);
			sdcardBean.setFri_1(-1);
			sdcardBean.setFri_2(-1);
			sdcardBean.setSat_0(-1);
			sdcardBean.setSat_1(-1);
			sdcardBean.setSat_2(-1);
		}

		sdcardBean.setRecord_timer(15);
		NativeCaller.PPPPSDRecordSetting(strDID,
				sdcardBean.getRecord_conver_enable(),
				sdcardBean.getRecord_timer(), sdcardBean.getRecord_size(),
				sdcardBean.getRecord_time_enable(), sdcardBean.getSun_0(),
				sdcardBean.getSun_1(), sdcardBean.getSun_2(),
				sdcardBean.getMon_0(), sdcardBean.getMon_1(),
				sdcardBean.getMon_2(), sdcardBean.getTue_0(),
				sdcardBean.getTue_1(), sdcardBean.getTue_2(),
				sdcardBean.getWed_0(), sdcardBean.getWed_1(),
				sdcardBean.getWed_2(), sdcardBean.getThu_0(),
				sdcardBean.getThu_1(), sdcardBean.getThu_2(),
				sdcardBean.getFri_0(), sdcardBean.getFri_1(),
				sdcardBean.getFri_2(), sdcardBean.getSat_0(),
				sdcardBean.getSat_1(), sdcardBean.getSat_2());

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		int i = v.getId();
		if (i == R.id.cbx_record_time) {
			if (isChecked) {
				sdcardBean.setRecord_time_enable(1);
			} else {
				sdcardBean.setRecord_time_enable(0);
			}

		} else {
		}

	}

	@Override
	public void callBackRecordSchParams(String did, int record_cover_enable,
			int record_timer, int record_size, int record_time_enable,
			int record_schedule_sun_0, int record_schedule_sun_1,
			int record_schedule_sun_2, int record_schedule_mon_0,
			int record_schedule_mon_1, int record_schedule_mon_2,
			int record_schedule_tue_0, int record_schedule_tue_1,
			int record_schedule_tue_2, int record_schedule_wed_0,
			int record_schedule_wed_1, int record_schedule_wed_2,
			int record_schedule_thu_0, int record_schedule_thu_1,
			int record_schedule_thu_2, int record_schedule_fri_0,
			int record_schedule_fri_1, int record_schedule_fri_2,
			int record_schedule_sat_0, int record_schedule_sat_1,
			int record_schedule_sat_2, int record_sd_status, int sdtotal,
			int sdfree) {
		Log.i("info", "---record_cover_enable" + record_cover_enable
				+ "---record_time_enable" + record_time_enable
				+ "---record_timer" + record_timer);
		Log.i("info", "record_schedule_sun_0:" + record_schedule_sun_0
				+ ",record_schedule_sun_1:" + record_schedule_sun_1
				+ ",record_schedule_sun_2:" + record_schedule_sun_2
				+ ",record_schedule_mon_0:" + record_schedule_mon_0
				+ ",record_schedule_mon_1:" + record_schedule_mon_1
				+ ",record_schedule_mon_2:" + record_schedule_mon_2);
		sdcardBean.setDid(did);
		sdcardBean.setRecord_conver_enable(record_cover_enable);
		sdcardBean.setRecord_timer(record_timer);
		sdcardBean.setRecord_size(record_size);
		sdcardBean.setRecord_time_enable(record_time_enable);
		sdcardBean.setRecord_sd_status(record_sd_status);
		sdcardBean.setSdtotal(sdtotal);
		sdcardBean.setSdfree(sdfree);
		sdcardBean.setSun_0(record_schedule_sun_0);
		sdcardBean.setSun_1(record_schedule_sun_1);
		sdcardBean.setSun_2(record_schedule_sun_2);
		sdcardBean.setMon_0(record_schedule_mon_0);
		sdcardBean.setMon_1(record_schedule_mon_1);
		sdcardBean.setMon_2(record_schedule_mon_2);
		sdcardBean.setTue_0(record_schedule_tue_0);
		sdcardBean.setTue_1(record_schedule_tue_1);
		sdcardBean.setTue_2(record_schedule_tue_2);
		sdcardBean.setWed_0(record_schedule_wed_0);
		sdcardBean.setWed_1(record_schedule_wed_1);
		sdcardBean.setWed_2(record_schedule_wed_2);
		sdcardBean.setThu_0(record_schedule_thu_0);
		sdcardBean.setThu_1(record_schedule_thu_1);
		sdcardBean.setThu_2(record_schedule_thu_2);
		sdcardBean.setFri_0(record_schedule_fri_0);
		sdcardBean.setFri_1(record_schedule_fri_1);
		sdcardBean.setFri_2(record_schedule_fri_2);
		sdcardBean.setSat_0(record_schedule_sat_0);
		sdcardBean.setSat_1(record_schedule_sat_1);
		sdcardBean.setSat_2(record_schedule_sat_2);
		handler.sendEmptyMessage(PARAMS);
	}

	@Override
	public void callBackSetSystemParamsResult(String did, int paramType,
			int result) {
		Log.d("tag", "result:" + result + " paramType:" + paramType);
		if (strDID.equals(did)) {
			handler.sendEmptyMessage(result);
		}
	}
}
