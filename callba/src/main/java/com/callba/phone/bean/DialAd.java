package com.callba.phone.bean;

import java.io.Serializable;

/**
 * Created by PC-20160514 on 2016/7/11.
 */
public class DialAd implements Serializable{
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
}
