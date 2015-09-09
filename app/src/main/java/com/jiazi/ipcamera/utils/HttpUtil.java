package com.jiazi.ipcamera.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 用来建立Http连接到某个网站
 */
public class HttpUtil {

    public static String connect(String webUrl) {

        HttpURLConnection connection = null;
        try {
            URL url = new URL(webUrl);             //创建URL对象
            connection = (HttpURLConnection) url.openConnection();
            //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
            connection.setConnectTimeout(4000); //设置连接超时为2秒
            connection.setRequestMethod("GET"); //设定请求方式
            connection.connect(); //建立到远程对象的实际连接
            //判断是否正常响应数据
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            InputStream is = connection.getInputStream();//返回打开连接读取的输入流
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String data = "";
            String readLine;
            while ((readLine = br.readLine()) != null) {
                data = data + readLine;
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect(); //中断连接
            }
        }
    }
}
