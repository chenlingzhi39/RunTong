package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.chat.EMGroupInfo;

/**
 * Created by PC-20160514 on 2016/10/22.
 */

public class GroupInfoAdapter extends RecyclerArrayAdapter<EMGroupInfo> {

    public GroupInfoAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupInfoViewHolder(parent);
    }
}
