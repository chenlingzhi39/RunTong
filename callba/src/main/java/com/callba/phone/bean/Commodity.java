package com.callba.phone.bean;

import java.util.ArrayList;

/**
 * Created by PC-20160514 on 2016/9/1.
 */
public class Commodity {
    String itemType;
    String iid;
    String price;
    String title;
    String imgUrl;
    String inTime;
    ArrayList<Coupon> coupon;
    ArrayList<Campaign> activity;

    public ArrayList<Campaign> getActivity() {
        return activity;
    }

    public void setActivity(ArrayList<Campaign> activity) {
        this.activity = activity;
    }

    public ArrayList<Coupon> getCoupon() {
        return coupon;
    }

    public void setCoupon(ArrayList<Coupon> coupon) {
        this.coupon = coupon;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
