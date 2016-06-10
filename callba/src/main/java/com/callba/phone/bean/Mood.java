package com.callba.phone.bean;

import java.util.ArrayList;

/**
 * Created by PC-20160514 on 2016/6/10.
 */
public class Mood {
    private int fid;
    private String content;
    private String inTime;
    private String imgUrls;
    private ArrayList<Dz> dz;
    private ArrayList<Comment> comment;

    public ArrayList<Comment> getComment() {
        return comment;
    }

    public void setComment(ArrayList<Comment> comment) {
        this.comment = comment;
    }

    public ArrayList<Dz> getDz() {
        return dz;
    }

    public void setDz(ArrayList<Dz> dz) {
        this.dz = dz;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public String getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(String imgUrls) {
        this.imgUrls = imgUrls;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }
}
