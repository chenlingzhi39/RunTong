package com.callba.phone.bean;

import java.io.Serializable;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class Team implements Serializable{
    private String url_head;
    private String nickname;
    private String inTime;
    private String title;

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
}
