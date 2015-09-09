package com.jiazi.ipcamera.bean;

/**
 * Created by Administrator on 2015/8/18.
 */
public class BluetoothdeviceBean {

    private String id;
    private String name;
    private String isBase;         //是否为蓝牙基站
    private String type;
    private String mac;
    private String xPos;
    private String yPos;
    private String time;

    public BluetoothdeviceBean(String id, String name ,String isBase, String type, String mac, String xPos, String yPos, String time) {
        this.id = id;
        this.name = name;
        this.isBase = isBase;
        this.type = type;
        this.mac = mac;
        this.xPos = xPos;
        this.time = time;
        this.yPos = yPos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsBase() {
        return isBase;
    }

    public void setIsBase(String isBase) {
        this.isBase = isBase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getxPos() {
        return xPos;
    }

    public void setxPos(String xPos) {
        this.xPos = xPos;
    }

    public String getyPos() {
        return yPos;
    }

    public void setyPos(String yPos) {
        this.yPos = yPos;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
