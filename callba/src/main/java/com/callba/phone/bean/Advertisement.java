package com.callba.phone.bean;

import java.io.Serializable;

/**
 * Created by PC-20160514 on 2016/6/14.
 */
public class Advertisement implements Serializable{
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

}
