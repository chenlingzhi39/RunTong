package com.callba.phone.ui.adapter;

import android.view.ViewGroup;

import com.callba.R;
import com.callba.phone.util.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/25.
 */

public class MemberViewHolder extends BaseViewHolder<String> {
    @BindView(R.id.avatar)
    CircleImageView avatar;

    public MemberViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_member);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(String data) {
        EaseUserUtils.setUserAvatar(getContext(),data,avatar);
    }
}
