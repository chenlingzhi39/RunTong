package com.callba.phone.bean;

/**
 * Created by Administrator on 2016/10/30.
 */

public class Tab {
    private int image,title;

    public Tab(int image, int title) {
        this.image = image;
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }
}
