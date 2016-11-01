package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-20160514 on 2016/6/14.
 */
public class Advertisement implements Parcelable{
    private String image;
    private String adurl;

    public String getAdurl() {
        return adurl;
    }

    public void setAdurl(String adurl) {
        this.adurl = adurl;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.image);
        dest.writeString(this.adurl);
    }

    public Advertisement() {
    }

    protected Advertisement(Parcel in) {
        this.image = in.readString();
        this.adurl = in.readString();
    }

    public static final Creator<Advertisement> CREATOR = new Creator<Advertisement>() {
        @Override
        public Advertisement createFromParcel(Parcel source) {
            return new Advertisement(source);
        }

        @Override
        public Advertisement[] newArray(int size) {
            return new Advertisement[size];
        }
    };
}
