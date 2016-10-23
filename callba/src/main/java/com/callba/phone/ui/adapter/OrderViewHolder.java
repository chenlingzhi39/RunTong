package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.bean.Order;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class OrderViewHolder extends BaseViewHolder<Order> {
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.state)
    TextView state;

    public OrderViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_order);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(Order data) {
        Glide.with(getContext()).load(data.getImgUrl()).placeholder(R.drawable.logo).into(image);
        name.setText(data.getTitle());
        time.setHint(data.getInTime());
        state.setHint(data.getState()==0?"未支付":"已支付");
    }
}
