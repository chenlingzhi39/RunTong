package com.callba.phone.bean;

import java.io.Serializable;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class Order implements Serializable{
    private String title;
    private String imgUrl;
    private String inTime;
    private int state;
    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
