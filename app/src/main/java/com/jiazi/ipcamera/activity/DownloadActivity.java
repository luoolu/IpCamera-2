package com.jiazi.ipcamera.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.service.DownloadService;

import is.arontibo.library.ElasticDownloadView;

/**
 * 更新下载界面
 */
public class DownloadActivity extends AppCompatActivity {

    private ElasticDownloadView mElasticDownloadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mElasticDownloadView = (ElasticDownloadView) findViewById(R.id.elastic_download_view);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.UPDATE_APK_START);
        filter.addAction(DownloadService.UPDATE_APK_UPDATE);
        filter.addAction(DownloadService.UPDATE_APK_FINISH);
        registerReceiver(mReceiver, filter);// 代码注册Receiver
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case DownloadService.UPDATE_APK_UPDATE:         //更新下载进度
                    mElasticDownloadView.startIntro();         //初始化控件
                    int progress = intent.getIntExtra("progress", 0);
                    int max = intent.getIntExtra("max", 100);
                    mElasticDownloadView.setProgress(progress * 100 / max);
                    break;
                case DownloadService.UPDATE_APK_FINISH:         //下载完成
                    mElasticDownloadView.success();
                    break;
                case DownloadService.UPDATE_APK_FAIL:         //下载失败
                    mElasticDownloadView.fail();
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
