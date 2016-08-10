package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by PC-20160514 on 2016/6/10.
 */
public class ImageAdapter extends RecyclerArrayAdapter<String>{
    public ImageAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }
}
