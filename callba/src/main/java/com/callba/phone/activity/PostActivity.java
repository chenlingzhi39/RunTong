package com.callba.phone.activity;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

/**
 * Created by PC-20160514 on 2016/5/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.post,
        navigationId = R.drawable.press_back,
        menuId=R.menu.menu_post
)
public class PostActivity extends BaseActivity{
    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
