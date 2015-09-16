package com.jiazi.ipcamera.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.asyncTask.FeedbackAsyncTask;
import com.jiazi.ipcamera.asyncTask.UpdateAsyncTask;
import com.jiazi.ipcamera.utils.UpdateLogUtils;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String companyWebsite = "http://www.jiazi-it.com/";
    private Toolbar mToolbar;
    private LinearLayout webLinearLayout;
    private LinearLayout logLinearLayout;
    private LinearLayout updateLinearLayout;
    private LinearLayout feedbackLinearLayout;
    private TextView versionTv;
    private String versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        logLinearLayout = (LinearLayout) findViewById(R.id.ll_updatelog);
        logLinearLayout.setOnClickListener(this);
        webLinearLayout = (LinearLayout) findViewById(R.id.ll_website);
        webLinearLayout.setOnClickListener(this);
        updateLinearLayout = (LinearLayout) findViewById(R.id.ll_update_version);
        updateLinearLayout.setOnClickListener(this);
        feedbackLinearLayout = (LinearLayout) findViewById(R.id.ll_feedback);
        feedbackLinearLayout.setOnClickListener(this);
        versionTv = (TextView) findViewById(R.id.tv_app_version);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_about);
        mToolbar.setTitle("关于");                                         //设置toolbar的标题
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);        //设置toolbar上的后退箭头
        }

        try {
            getVersion();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        versionTv.setText(versionCode);
    }

    /**
     * 取得APP的版本备注
     */
    private void getVersion() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
        versionCode = packageInfo.versionName;          //取得APP的版本号
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ll_updatelog:
                UpdateLogUtils updateLogUtils = new UpdateLogUtils(this);
                updateLogUtils.showUpdateLog();
                break;
            case R.id.ll_website:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {  //判断安卓版本是否小于4.2.2
                    Uri uri = Uri.parse(companyWebsite);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AboutActivity.this, WebActivity.class);         //跳转到网页
                    startActivity(intent);
                }
                break;
            case R.id.ll_update_version:
                UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this);
                updateAsyncTask.execute();
                break;
            case R.id.ll_feedback:
                showFeedbackDialog();
            default:
                break;
        }
    }

    /**
     * 显示添加反馈信息的对话框
     */
    private void showFeedbackDialog() {
        new MaterialDialog.Builder(this)
                .title("意见反馈")
                .content("提交bug或者提出新功能建议")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText("提交").positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText("取消").negativeColor(getResources().getColor(R.color.colorPrimary))
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (TextUtils.isEmpty(input)) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        } else {
                            FeedbackAsyncTask feedbackAsyncTask = new FeedbackAsyncTask(AboutActivity.this, input.toString(), dialog);
                            feedbackAsyncTask.execute();
                        }
                    }
                }).show();
    }
}
