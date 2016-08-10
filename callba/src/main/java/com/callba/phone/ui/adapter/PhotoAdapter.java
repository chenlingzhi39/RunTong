package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by PC-20160514 on 2016/6/6.
 */
public class PhotoAdapter extends RecyclerArrayAdapter<String>{
    public PhotoAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(parent);
    }
}
