package com.callba.phone.adapter;

import android.content.Context;

import com.callba.phone.bean.Flow;

import java.util.List;

/**
 * Created by PC-20160514 on 2016/7/26.
 */
public class FlowAdapter extends RadioAdapter<Flow>{
    public FlowAdapter(Context context, List<Flow> items) {
        super(context, items);
    }

    @Override
    public void onBindViewHolder(RadioAdapter.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        viewHolder.mRadio.setText(mItems.get(i).getFlowValue());
    }
}
