package com.callba.phone.bean;

/**
 * Created by PC-20160514 on 2016/9/5.
 */
public class HomeItem {
    private int res;
    private String name;
    private boolean is_discount;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public boolean is_discount() {
        return is_discount;
    }

    public void setIs_discount(boolean is_discount) {
        this.is_discount = is_discount;
    }

    public HomeItem(String name, int res) {
        this.name = name;
        this.res = res;
    }
}
