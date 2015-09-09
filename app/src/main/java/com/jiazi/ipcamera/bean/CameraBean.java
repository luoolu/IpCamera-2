package com.jiazi.ipcamera.bean;

/**
 * 摄像头信息实体类
 */
public class CameraBean {
    private String did;
    private String name;
    private String psw;
    private String nickname;
    private String alarminfo;
    private String mac;
    private float xStart;
    private float xStop;
    private float yStart;
    private float yStop;

    public CameraBean(String did, String name, String psw, String nickname,
                      String alarminfo, String mac, float xStart, float xStop, float yStart, float yStop) {
        this.did = did;
        this.name = name;
        this.psw = psw;
        this.nickname = nickname;
        this.alarminfo = alarminfo;
        this.mac = mac;
        this.xStart = xStart;
        this.xStop = xStop;
        this.yStart = yStart;
        this.yStop = yStop;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getAlarminfo() {
        return alarminfo;
    }

    public void setAlarminfo(String alarminfo) {
        this.alarminfo = alarminfo;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public float getxStart() {
        return xStart;
    }

    public void setxStart(float xStart) {
        this.xStart = xStart;
    }

    public float getxStop() {
        return xStop;
    }

    public void setxStop(float xStop) {
        this.xStop = xStop;
    }

    public float getyStart() {
        return yStart;
    }

    public void setyStart(float yStart) {
        this.yStart = yStart;
    }

    public float getyStop() {
        return yStop;
    }

    public void setyStop(float yStop) {
        this.yStop = yStop;
    }
}
