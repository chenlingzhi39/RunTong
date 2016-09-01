package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Campaign;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/9/1.
 */
public class CampaignViewHolder extends BaseViewHolder<Campaign> {
    @InjectView(R.id.content)
    TextView content;

    public CampaignViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_campaign);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Campaign data) {
      content.setText((getAdapterPosition()+1)+"."+data.getContent());
    }
}
