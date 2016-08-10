package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Mood;

/**
 * Created by PC-20160514 on 2016/6/6.
 */
public class MoodAdapter extends RecyclerArrayAdapter<Mood>{
    public MoodAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoodViewHolder(parent);
    }
}
