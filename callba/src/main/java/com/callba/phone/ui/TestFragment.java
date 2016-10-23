package com.callba.phone.ui;

import android.view.View;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseFragment;

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

}
