package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by PC-20160514 on 2016/10/25.
 */

public class MemberAdapter extends RecyclerArrayAdapter<String> {
    public MemberAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MemberViewHolder(parent);
    }
}
