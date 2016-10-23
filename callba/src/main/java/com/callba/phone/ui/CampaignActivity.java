package com.callba.phone.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.callba.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/9/5.
 */
public class CampaignActivity extends Activity {
    @BindView(R.id.image)
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign);
        ButterKnife.bind(this);
        if(!TextUtils.isEmpty(getIntent().getStringExtra("image")))
       Glide.with(this).load(getIntent().getStringExtra("image")).into(image);
    }

    @OnClick(R.id.close)
    public void onClick() {
        finish();
    }
}
