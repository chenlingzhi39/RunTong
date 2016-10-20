package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.SeparatedEMGroup;
import com.hyphenate.chat.EMGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class GroupViewHolder extends BaseViewHolder<SeparatedEMGroup> {
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.name)
    TextView name;

    public GroupViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_group);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(SeparatedEMGroup data) {
       name.setText(data.getEmGroup().getGroupName());
    }
}
