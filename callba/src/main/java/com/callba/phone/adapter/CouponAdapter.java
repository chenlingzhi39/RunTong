package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Coupon;

/**
 * Created by PC-20160514 on 2016/7/30.
 */
public class CouponAdapter extends RecyclerArrayAdapter<Coupon>{
    public CouponAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new CouponViewHolder(parent);
    }
}
