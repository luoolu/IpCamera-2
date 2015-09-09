package com.jiazi.ipcamera.bean;

import java.io.Serializable;

/**
 * App信息实体类
 */
public class AppBean implements Serializable {
    public int id;
    public int versionCode;
    public String versionName;
    public String url;
    public String content;
    public int size;
    public String fileName;
    public String time;
}
