package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.bean.NearByUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/24.
 */
public class NearByUserViewHolder extends BaseViewHolder<NearByUser> {
    @InjectView(R.id.user_head)
    CircleImageView userHead;
    @InjectView(R.id.user_name)
    TextView userName;
    @InjectView(R.id.distance)
    TextView distance;

    public NearByUserViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_nearby_user);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(NearByUser data) {
       userName.setText(data.getNickname());
        if(!data.getUrl_head().equals(""))
        Glide.with(getContext()).load(data.getUrl_head()).into(userHead);
        distance.setHint(data.getDistance().substring(0,data.getDistance().lastIndexOf("."))+"米以内");
    }
}
