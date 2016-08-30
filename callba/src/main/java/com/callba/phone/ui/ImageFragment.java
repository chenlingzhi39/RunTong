package com.callba.phone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/8/30.
 */
@ActivityFragmentInject(contentViewId = R.layout.fragment_image)
public class ImageFragment extends BaseFragment {
    @InjectView(R.id.image)
    ImageView image;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        if(getArguments().getInt("id")!=0)
            Glide.with(this).load(getArguments().getInt("id")).into(image);
        //image.setImageResource(getArguments().getInt("id"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
