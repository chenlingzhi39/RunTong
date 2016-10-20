package com.callba.phone.bean;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.adapter.EMAGroup;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class SeparatedEMGroup{

    private EMGroup emGroup;
    private String type;

    public SeparatedEMGroup(EMGroup emGroup, String type) {
        this.emGroup = emGroup;
        this.type = type;
    }

    public EMGroup getEmGroup() {
        return emGroup;
    }

    public void setEmGroup(EMGroup emGroup) {
        this.emGroup = emGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
