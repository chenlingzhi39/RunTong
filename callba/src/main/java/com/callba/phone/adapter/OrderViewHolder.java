package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.bean.Order;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class OrderViewHolder extends BaseViewHolder<Order> {
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.state)
    TextView state;

    public OrderViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_order);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Order data) {
        Glide.with(getContext()).load(data.getImgUrl()).placeholder(R.drawable.logo).into(image);
        name.setText(data.getTitle());
        time.setHint(data.getInTime());
        state.setHint(data.getState()==0?"未支付":"已支付");
    }
}
