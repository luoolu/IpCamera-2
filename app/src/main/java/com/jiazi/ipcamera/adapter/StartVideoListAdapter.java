package com.jiazi.ipcamera.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jiazi.ipcamera.activity.CameraActivity;
import com.jiazi.ipcamera.asyncTask.CancelAlertAsyncTask;
import com.jiazi.ipcamera.bean.CameraBean;
import com.jiazi.ipcamera.fragment.CameraFragment;
import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.utils.CameraManager;

import java.util.List;

/**
 * Created by Administrator on 2015/7/29.
 */
public class StartVideoListAdapter extends RecyclerView.Adapter<StartVideoListAdapter.ViewHolder> {

    private List<CameraBean> mCameras;

    private Context mContext;

    private CameraManager mCameraManager;


    public StartVideoListAdapter(Context context, List<CameraBean> cameras, CameraManager mCameraManager) {
        this.mContext = context;
        this.mCameras = cameras;
        this.mCameraManager = mCameraManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // 给ViewHolder设置布局文件
        View v = LayoutInflater.from(mContext).inflate(R.layout.recycler_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {
        // 给ViewHolder设置元素
        final CameraBean camera = mCameras.get(i);
        holder.uidText.setText(camera.getDid());
        holder.devNameText.setText(camera.getNickname());

        if (camera.getAlarminfo() != null) {
            holder.devsafeIv.setImageResource(R.drawable.ic_warning);
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CameraActivity.class);
                String uid = camera.getDid();
                String username = camera.getName();
                String password = camera.getPsw();
                String devName = camera.getNickname();
                intent.putExtra("uid", uid);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                intent.putExtra("devName", devName);
                CameraFragment.fromMapActivity = false;
                mContext.startActivity(intent);
            }
        });
        String alarminfo = mCameras.get(i).getAlarminfo();
        if (alarminfo != null) {
            holder.mCardView.setLongClickable(true);
            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
                    builder.title("提示")
                            .content("确定取消警告？")
                            .positiveText("确定").positiveColor(mContext.getResources().getColor(R.color.colorPrimary))
                            .negativeText("取消").negativeColor(mContext.getResources().getColor(R.color.colorPrimary))
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    String mac = mCameras.get(i).getMac();
                                    CancelAlertAsyncTask cancelAlertAsyncTask = new CancelAlertAsyncTask(mContext, mac, camera, mCameraManager, holder.devsafeIv);
                                    cancelAlertAsyncTask.execute();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .show();
                    return false;
                }
            });
        } else {
            holder.mCardView.setLongClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        // 返回数据总数
        return mCameras == null ? 0 : mCameras.size();
    }

    // 重写的自定义ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView uidText;
        private TextView devNameText;
        private ImageView devsafeIv;

        public ViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            uidText = (TextView) v.findViewById(R.id.tv_dev_uid);
            devNameText = (TextView) v.findViewById(R.id.tv_dev_name);
            devsafeIv = (ImageView) v.findViewById(R.id.iv_camera_info);
        }
    }

}
