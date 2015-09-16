package com.jiazi.ipcamera.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络连接帮助类
 */
public class HttpUtil {

    public static String getData(String webUrl) {

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

    /**
     * 发送Post请求到服务器
     */
    public static String postData(String path, String data) throws IOException {
        // 提交数据到服务器
        // 拼装路径
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");

        // 准备数据
        byte[] bytes = data.getBytes();
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//设置请求体的类型是文本类型
        conn.setRequestProperty("Content-Length", bytes.length + "");//设置请求体的长度

        conn.setDoOutput(true);        //设置为输出
        OutputStream os = conn.getOutputStream();
        os.write(bytes);

        int code = conn.getResponseCode();
        if (code == 200) {
            // 请求成功
            InputStream is = conn.getInputStream();
            return HttpUtil.readInputStreaam(is);
        } else {
            return null;
        }
    }

    /**
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String readInputStreaam(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
}
