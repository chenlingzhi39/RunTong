package com.callba.phone.ui.adapter;


import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Member;
import com.callba.phone.util.EaseUserUtils;
import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/25.
 */

public class GroupMemberViewHolder extends BaseViewHolder<Member> {
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.me)
    TextView me;

    public GroupMemberViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_group_member);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(Member data) {
        EaseUserUtils.setUserNick(data.getName(), name);
        EaseUserUtils.setUserAvatar(getContext(), data.getName(), avatar);
        if (EMClient.getInstance().getCurrentUser().equals(data.getName()))
          me.setVisibility(View.VISIBLE);
        else me.setVisibility(View.GONE);
    }
}
