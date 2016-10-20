package com.callba.phone.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.HomeItem;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by PC-20160514 on 2016/9/5.
 */
public class HomeViewHolder extends BaseViewHolder<HomeItem> {

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.is_discount)
    ImageView isDiscount;

    public HomeViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_home);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(HomeItem data) {
        image.setImageResource(data.getRes());
        text.setText(data.getName());
     isDiscount.setVisibility(data.is_discount()? View.VISIBLE:View.GONE);
    }
}
