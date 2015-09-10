package com.jiazi.ipcamera.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.asyncTask.UpdateAsyncTask;
import com.jiazi.ipcamera.utils.UpdateLogUtils;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private LinearLayout webLinearLayout;
    private LinearLayout logLinearLayout;
    private LinearLayout updateLinearLayout;
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
                Uri uri = Uri.parse("http://www.jiazi-it.com/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);         //跳转到网页
                startActivity(intent);
                break;
            case R.id.ll_update_version:
                UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this);
                updateAsyncTask.execute();
                break;
            default:
                break;
        }
    }
}
