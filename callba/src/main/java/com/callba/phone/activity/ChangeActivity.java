package com.callba.phone.activity;

import android.os.Bundle;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

/**
 * Created by PC-20160514 on 2016/5/19.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.change,
        toolbarTitle = R.string.change,
        navigationId=R.drawable.press_back
)
public class ChangeActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
