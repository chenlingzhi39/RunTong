package com.callba.phone.ui.adapter;


import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Member;
import com.callba.phone.util.EaseUserUtils;

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

    public GroupMemberViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_contact);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(Member data) {
        EaseUserUtils.setUserNick(data.getName(),name);
        EaseUserUtils.setUserAvatar(getContext(),data.getName(),avatar);
    }
}
