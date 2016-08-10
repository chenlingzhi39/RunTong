package com.callba.phone.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.ui.FlowActivity;
import com.callba.phone.bean.Coupon;
import com.callba.phone.ui.SelectFriendActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/30.
 */
public class CouponViewHolder extends BaseViewHolder<Coupon> {
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.content)
    TextView content;
    @InjectView(R.id.get_flow)
    Button getFlow;
    @InjectView(R.id.give_flow)
    Button giveFlow;
    @InjectView(R.id.title)
    TextView title;

    public CouponViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_coupon);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(final Coupon data) {
        Glide.with(getContext()).load(data.getImgUrl()).into(image);
        content.setText(data.getContent());
        title.setText(data.getTitle());
        getFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FlowActivity.class);
                intent.putExtra("index", 1);
                intent.putExtra("coupon",data);
                ((Activity)getContext()).startActivityForResult(intent,0);
            }
        });
        giveFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SelectFriendActivity.class);
                intent.putExtra("coupon",data);
                ((Activity)getContext()).startActivityForResult(intent,0);
            }
        });
    }
}
