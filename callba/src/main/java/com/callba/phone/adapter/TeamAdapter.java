package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Team;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class TeamAdapter extends RecyclerArrayAdapter<Team>{
    public TeamAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new TeamViewHolder(parent);
    }
}
