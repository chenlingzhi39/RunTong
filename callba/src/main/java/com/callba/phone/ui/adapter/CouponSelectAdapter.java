package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.callba.R;
import com.callba.phone.bean.Coupon;

import java.util.List;

/**
 * Created by PC-20160514 on 2016/9/1.
 */
public class CouponSelectAdapter extends RadioAdapter<Coupon>{
    public CouponSelectAdapter(Context context, List<Coupon> items) {
        super(context, items);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.item_select_coupon, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RadioAdapter.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        viewHolder.mRadio.setText(mItems.get(i).getTitle());
    }
}
