package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.callba.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/6/10.
 */
public class ImageViewHolder extends BaseViewHolder<String> {
    @BindView(R.id.image)
    ImageView image;

    public ImageViewHolder(ViewGroup parent) {
        super(parent, R.layout.image);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(String data) {
        Glide.with(getContext()).load(data).into(image);
    }
}
