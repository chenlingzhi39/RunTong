package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Order;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class OrderAdapter extends RecyclerArrayAdapter<Order>{
    public OrderAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrderViewHolder(parent);
    }
}
