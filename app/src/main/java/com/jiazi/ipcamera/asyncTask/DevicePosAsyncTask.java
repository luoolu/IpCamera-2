package com.jiazi.ipcamera.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jiazi.ipcamera.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 根据返回的网站的数据设置摄像头的位置信息的后台线程
 */
public class DevicePosAsyncTask extends AsyncTask<String, Void, String[]> {

    private Context mContext;
    private String mUID;
    private String website;
    private TextView tvPosX;
    private TextView tvPosY;


    public DevicePosAsyncTask(Context mContext, String uid, String website, TextView tvPosX, TextView tvPosY) {
        this.mContext = mContext;
        mUID = uid;
        this.website = website;
        this.tvPosX = tvPosX;
        this.tvPosY = tvPosY;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if (result != null) {
            tvPosX.setVisibility(View.VISIBLE);
            tvPosY.setVisibility(View.VISIBLE);
            tvPosX.setText("横轴监控位置： " + result[0]);
            tvPosY.setText("纵轴监控位置： " + result[1]);
        } else {
            tvPosX.setVisibility(View.VISIBLE);
            tvPosY.setVisibility(View.VISIBLE);
            tvPosX.setText("未查询到横轴监控位置");
            tvPosY.setText("未查询到纵轴监控位置");
        }
    }

    @Override
    protected String[] doInBackground(String... strings) {
        String result = HttpUtil.getData(website);
        JSONObject object;
        if (result != null) {
            try {
                object = new JSONObject(result);
                /**
                 * 在你获取的string这个JSON对象中，提取你所需要的信息。
                 */
                String resultCode = object.getString("code");
                if (resultCode.equals("301")) {    //查找失败
                    Toast.makeText(mContext, "监控位置查找失败", Toast.LENGTH_SHORT).show();
                } else if (resultCode.equals("300")) {                 //查找成功
                    JSONArray datas = object.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject data = (JSONObject) datas.get(i);
                        String uid = data.getString("uid");
                        String xstartPos = data.getString("x_start");
                        String xendPos = data.getString("x_end");
                        String ystartPos = data.getString("y_start");
                        String yendPos = data.getString("y_end");
                        String xPos = xstartPos + " ~ " + xendPos;
                        String yPos = ystartPos + " ~ " + yendPos;
                        String[] position = new String[]{xPos, yPos};
                        if (uid.equals(mUID)) {
                            return position;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
