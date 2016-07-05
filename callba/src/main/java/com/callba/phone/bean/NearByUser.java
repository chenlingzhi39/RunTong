package com.callba.phone.bean;

/**
 * Created by Administrator on 2016/5/21.
 */
public class NearByUser {
    private String userID;
    private String nickname;
    private String phoneNumber;
    private String url_head;
    private String distance;
    private String sign;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickName) {
        this.nickname = nickName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUrl_head() {
        return url_head;
    }

    public void setUrl_head(String url_head) {
        this.url_head = url_head;
    }
}
