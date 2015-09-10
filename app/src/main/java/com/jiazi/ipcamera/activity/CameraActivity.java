package com.jiazi.ipcamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.fragment.CameraFragment;
import com.jiazi.ipcamera.utils.ContentCommon;
import com.jiazi.ipcamera.utils.SystemValue;

/**
 * 摄像头具体界面以及设置界面各功能的入口
 */
public class CameraActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private NavigationView mNavigationView;
    private CameraFragment mCameraFragment;
    private SharedPreferences mSharedPreferences;

    private boolean isFirstOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        mCameraFragment = new CameraFragment();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().add(R.id.container, mCameraFragment).commit();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_menu);

        setSupportActionBar(mToolBar);           //设置ToolBar为ActionBar
        final ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        setupDrawerContent(mNavigationView);

        mSharedPreferences = getSharedPreferences("SharedPreferences", Activity.MODE_PRIVATE);
        isFirstOpen = mSharedPreferences.getBoolean("firstOpen", true);

        if (isFirstOpen) {            //如果第一次进入此界面会提示右滑打开菜单
            ShowcaseView showcaseView = new ShowcaseView.Builder(this)
                    .setStyle(R.style.Custom_semi_transparent)//setStyle instead of setTarget!
                    .hideOnTouchOutside()
                    .build();
            showcaseView.setBackground(getResources().getDrawable(R.drawable.swipe_back_en));//deprecated.
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("firstOpen", false);
            editor.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 监听NavigationView中item的点击事项
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {
                    private MenuItem mPreMenuItem;
                    private int itemId;

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        if (mPreMenuItem != null) {
                            mPreMenuItem.setChecked(false);
                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mPreMenuItem = menuItem;
                        itemId = menuItem.getItemId();

                        if (itemId == R.id.nav_play) {
                            Intent intent = new Intent(CameraActivity.this, PlayActivity.class);
                            startActivity(intent);

                        } else if (itemId == R.id.nav_local_video) {
                            Intent intent1 = new Intent(CameraActivity.this,
                                    LocalPictureAndVideoActivity.class);
                            intent1.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent1.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent1.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent1);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.play_back) {
                            Intent intent2 = new Intent(CameraActivity.this,
                                    PlayBackTFActivity.class);
                            intent2.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent2.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent2.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent2);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.nav_wifi_setting) {
                            Intent intent3 = new Intent(CameraActivity.this, SettingWifiActivity.class);
                            intent3.putExtra(ContentCommon.STR_CAMERA_ID, SystemValue.deviceId);
                            intent3.putExtra(ContentCommon.STR_CAMERA_NAME, SystemValue.deviceName);
                            intent3.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent3);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.nav_change_info) {
                            Intent intent4 = new Intent(CameraActivity.this,
                                    SettingUserActivity.class);
                            intent4.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent4.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent4.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent4);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.alert_setting) {
                            Intent intent5 = new Intent(CameraActivity.this,
                                    SettingAlarmActivity.class);
                            intent5.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent5.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent5.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent5);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.time_setting) {
                            Intent intent6 = new Intent(CameraActivity.this,
                                    SettingDateActivity.class);
                            intent6.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent6.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent6.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent6);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        } else if (itemId == R.id.sdcard_setting) {
                            Intent intent7 = new Intent(CameraActivity.this,
                                    SettingSDCardActivity.class);
                            intent7.putExtra(ContentCommon.STR_CAMERA_ID,
                                    SystemValue.deviceId);
                            intent7.putExtra(ContentCommon.STR_CAMERA_NAME,
                                    SystemValue.deviceName);
                            intent7.putExtra(ContentCommon.STR_CAMERA_PWD, SystemValue.devicePass);
                            startActivity(intent7);
                            overridePendingTransition(R.anim.in_from_right,
                                    R.anim.out_to_left);

                        }
                        return true;
                    }
                }

        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
