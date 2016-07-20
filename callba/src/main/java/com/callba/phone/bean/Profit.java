package com.callba.phone.bean;

import java.io.Serializable;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class Profit implements Serializable{
    private String money;
    private String inTime;

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
