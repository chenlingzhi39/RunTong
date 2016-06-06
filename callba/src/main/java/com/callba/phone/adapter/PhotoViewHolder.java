package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.callba.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/6.
 */
public class PhotoViewHolder extends BaseViewHolder<String> {
    @InjectView(R.id.image)
    ImageView image;

    public PhotoViewHolder(ViewGroup parent) {
        super(parent, R.layout.photo_item);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(String data) {
        Glide.with(getContext()).load("file://"+data).into(image);
    }
}
