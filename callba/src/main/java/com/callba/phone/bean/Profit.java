package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class Profit implements Parcelable{
    private String money;
    private String inTime;

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.money);
        dest.writeString(this.inTime);
    }

    public Profit() {
    }

    protected Profit(Parcel in) {
        this.money = in.readString();
        this.inTime = in.readString();
    }

    public static final Creator<Profit> CREATOR = new Creator<Profit>() {
        @Override
        public Profit createFromParcel(Parcel source) {
            return new Profit(source);
        }

        @Override
        public Profit[] newArray(int size) {
            return new Profit[size];
        }
    };
}
