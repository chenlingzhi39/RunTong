package com.callba.phone.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/2.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.settings,
        toolbarTitle = R.string.settings,
        navigationId = R.drawable.press_back
)
public class SettingsActivity extends BaseActivity {
    @BindView(R.id.fragment)
    FrameLayout fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragment,  SettingsFragment.newInstance());

        transaction.commit();
    }

}
