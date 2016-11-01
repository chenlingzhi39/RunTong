package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-20160514 on 2016/7/11.
 */
public class DialAd implements Parcelable{
    private String image;
    private String adWavUrl;

    public String getAdWavUrl() {
        return adWavUrl;
    }

    public void setAdWavUrl(String adWavUrl) {
        this.adWavUrl = adWavUrl;
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
        dest.writeString(this.adWavUrl);
    }

    public DialAd() {
    }

    protected DialAd(Parcel in) {
        this.image = in.readString();
        this.adWavUrl = in.readString();
    }

    public static final Creator<DialAd> CREATOR = new Creator<DialAd>() {
        @Override
        public DialAd createFromParcel(Parcel source) {
            return new DialAd(source);
        }

        @Override
        public DialAd[] newArray(int size) {
            return new DialAd[size];
        }
    };
}
