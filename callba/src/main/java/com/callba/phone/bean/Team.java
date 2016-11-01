package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class Team implements Parcelable{
    private String url_head;
    private String nickname;
    private String inTime;
    private String title;
    private String phoneNumber;
    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl_head() {
        return url_head;
    }

    public void setUrl_head(String url_head) {
        this.url_head = url_head;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url_head);
        dest.writeString(this.nickname);
        dest.writeString(this.inTime);
        dest.writeString(this.title);
        dest.writeString(this.phoneNumber);
    }

    public Team() {
    }

    protected Team(Parcel in) {
        this.url_head = in.readString();
        this.nickname = in.readString();
        this.inTime = in.readString();
        this.title = in.readString();
        this.phoneNumber = in.readString();
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel source) {
            return new Team(source);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };
}
