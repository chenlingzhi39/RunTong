package com.callba.phone.bean;

/**
 * Created by PC-20160514 on 2016/7/26.
 */
public class Flow {
    String name;
    String past_price_local;
    String now_price_local;
    String past_price_nation;
    String now_price_nation;

    public Flow(String name, String now_price_local, String now_price_nation, String past_price_local, String past_price_nation) {
        this.name = name;
        this.now_price_local = now_price_local;
        this.now_price_nation = now_price_nation;
        this.past_price_local = past_price_local;
        this.past_price_nation = past_price_nation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNow_price_local() {
        return now_price_local;
    }

    public void setNow_price_local(String now_price_local) {
        this.now_price_local = now_price_local;
    }

    public String getPast_price_local() {
        return past_price_local;
    }

    public void setPast_price_local(String past_price_local) {
        this.past_price_local = past_price_local;
    }

    public String getNow_price_nation() {
        return now_price_nation;
    }

    public void setNow_price_nation(String now_price_nation) {
        this.now_price_nation = now_price_nation;
    }

    public String getPast_price_nation() {
        return past_price_nation;
    }

    public void setPast_price_nation(String past_price_nation) {
        this.past_price_nation = past_price_nation;
    }
}
