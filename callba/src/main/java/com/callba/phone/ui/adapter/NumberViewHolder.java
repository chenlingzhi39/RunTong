package com.callba.phone.ui.adapter;

import android.view.ViewGroup;

import com.callba.R;

import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/6/20.
 */
public class NumberViewHolder extends BaseViewHolder<String>{
    public NumberViewHolder(ViewGroup parent) {
        super(parent, R.layout.contact_detail_lv_item);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(String data) {
        super.setData(data);
    }
}
