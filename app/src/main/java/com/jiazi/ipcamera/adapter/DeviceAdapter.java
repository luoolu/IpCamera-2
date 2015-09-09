package com.jiazi.ipcamera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jiazi.ipcamera.R;
import com.jiazi.ipcamera.bean.BluetoothdeviceBean;

import java.util.List;

/**
 * 蓝牙设备listview的内容适配器
 */
public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothdeviceBean> devices;
    private LayoutInflater mLayoutInflater;

    public DeviceAdapter(Context context, List<BluetoothdeviceBean> devices, LayoutInflater layoutInflater) {
        mContext = context;
        this.devices = devices;
        mLayoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.listview_content, null);
            mViewHolder = new ViewHolder();
            mViewHolder.mTvDeviceId = (TextView) convertView.findViewById(R.id.tv_device_id);
            mViewHolder.mTvDeviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            mViewHolder.mTvDeviceEnterTime = (TextView) convertView.findViewById(R.id.tv_device_enter_time);
            mViewHolder.mTvDeviceLastTime = (TextView) convertView.findViewById(R.id.tv_device_last_time);
            mViewHolder.mTvDeviceInfo = (TextView) convertView.findViewById(R.id.tv_device_info);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothdeviceBean mDevice = devices.get(position);
        mViewHolder.mTvDeviceId.setText(mDevice.getId());
        mViewHolder.mTvDeviceName.setText(mDevice.getName());
        mViewHolder.mTvDeviceInfo.setText(mDevice.getType());
        mViewHolder.mTvDeviceEnterTime.setText("--");
        mViewHolder.mTvDeviceLastTime.setText("--");
        return convertView;
    }

    public class ViewHolder {
        public TextView mTvDeviceId;
        public TextView mTvDeviceName;
        public TextView mTvDeviceEnterTime;
        public TextView mTvDeviceLastTime;
        public TextView mTvDeviceInfo;

    }
}
