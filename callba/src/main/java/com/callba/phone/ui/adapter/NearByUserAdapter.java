package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.NearByUser;

/**
 * Created by PC-20160514 on 2016/5/24.
 */
public class NearByUserAdapter extends RecyclerArrayAdapter<NearByUser>{
    public NearByUserAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new NearByUserViewHolder(parent);
    }
}
