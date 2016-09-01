package com.callba.phone.bean;

/**
 * Created by PC-20160514 on 2016/7/26.
 */
public class Flow extends Commodity {
    String flowValue;
    String operators;
    String oldPrice;
    public String getFlowValue() {
        return flowValue;
    }

    public void setFlowValue(String flowValue) {
        this.flowValue = flowValue;
    }


    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getOperators() {
        return operators;
    }

    public void setOperators(String operators) {
        this.operators = operators;
    }


    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

}
