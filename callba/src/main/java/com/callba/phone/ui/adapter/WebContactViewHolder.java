package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.util.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class WebContactViewHolder extends BaseViewHolder<EaseUser> {
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.name)
    TextView name;

    public WebContactViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_contact);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(EaseUser data) {
        EaseUserUtils.setUserNick(data.getUsername(),name);
        EaseUserUtils.setUserAvatar(getContext(),data.getUsername(),avatar);
    }
}
