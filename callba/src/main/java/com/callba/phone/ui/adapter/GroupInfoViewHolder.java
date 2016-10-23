package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.hyphenate.chat.EMGroupInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/10/22.
 */

public class GroupInfoViewHolder extends BaseViewHolder<EMGroupInfo> {
    @BindView(R.id.name)
    TextView name;

    public GroupInfoViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_group);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(EMGroupInfo data) {
     name.setText(data.getGroupName());
    }
}
