package com.callba.phone.ui;

import android.os.Bundle;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseActivity;

/**
 * Created by PC-20160514 on 2016/10/10.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_tutorial
)
public class TutorialActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            replaceTutorialFragment();
        }
    }
    public void replaceTutorialFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new CustomTutorialSupportFragment())
                .commit();
    }
}
