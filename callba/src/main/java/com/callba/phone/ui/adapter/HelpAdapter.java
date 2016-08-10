package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Help;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
public class HelpAdapter extends RecyclerArrayAdapter<Help>{
    public HelpAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new HelpViewHolder(parent);
    }
}
