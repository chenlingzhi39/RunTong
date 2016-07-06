package com.callba.phone.activity;

import android.view.View;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.test
)
public class TestFragment extends BaseFragment{
    @Override
    protected void initView(View fragmentRootView) {

    }
    public static TestFragment newInstance() {
        TestFragment testFragment = new TestFragment();
        return testFragment;
    }

    @Override
    protected void lazyLoad() {

    }
}
