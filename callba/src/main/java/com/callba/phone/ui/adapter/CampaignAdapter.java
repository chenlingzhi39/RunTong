package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Campaign;

/**
 * Created by PC-20160514 on 2016/9/1.
 */
public class CampaignAdapter extends RecyclerArrayAdapter<Campaign>{
    public CampaignAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new CampaignViewHolder(parent);
    }
}
