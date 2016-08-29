package com.callba.phone.bean;

/**
 * Created by PC-20160514 on 2016/6/28.
 */
public class BaseUser {
private String phoneNumber;
private String nickname;
private String sign;
private String url_head;
private String remark;
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getUrl_head() {
        return url_head;
    }

    public void setUrl_head(String url_head) {
        this.url_head = url_head;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
