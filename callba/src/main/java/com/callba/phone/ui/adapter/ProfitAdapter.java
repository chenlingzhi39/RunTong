package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Profit;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class ProfitAdapter extends RecyclerArrayAdapter<Profit>{
    public ProfitAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProfitViewHolder(parent);
    }
}
