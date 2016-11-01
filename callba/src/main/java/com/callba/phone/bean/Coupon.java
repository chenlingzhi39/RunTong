package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-20160514 on 2016/7/30.
 */
public class Coupon implements Parcelable{
    String title;
    String imgUrl;
    String inTime;
    String state;
    String content;
    String iid;
    String iid2;
    String price;
    String type;
    String cid;

    public Coupon(String content) {
        this.title = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getIid2() {
        return iid2;
    }

    public void setIid2(String iid2) {
        this.iid2 = iid2;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.imgUrl);
        dest.writeString(this.inTime);
        dest.writeString(this.state);
        dest.writeString(this.content);
        dest.writeString(this.iid);
        dest.writeString(this.iid2);
        dest.writeString(this.price);
        dest.writeString(this.type);
        dest.writeString(this.cid);
    }

    protected Coupon(Parcel in) {
        this.title = in.readString();
        this.imgUrl = in.readString();
        this.inTime = in.readString();
        this.state = in.readString();
        this.content = in.readString();
        this.iid = in.readString();
        this.iid2 = in.readString();
        this.price = in.readString();
        this.type = in.readString();
        this.cid = in.readString();
    }

    public static final Creator<Coupon> CREATOR = new Creator<Coupon>() {
        @Override
        public Coupon createFromParcel(Parcel source) {
            return new Coupon(source);
        }

        @Override
        public Coupon[] newArray(int size) {
            return new Coupon[size];
        }
    };
}
