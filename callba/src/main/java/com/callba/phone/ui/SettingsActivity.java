package com.callba.phone.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/2.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.settings,
        toolbarTitle = R.string.settings,
        navigationId = R.drawable.press_back,
        menuId=R.menu.settings
)
public class SettingsActivity extends BaseActivity {
    @InjectView(R.id.fragment)
    FrameLayout fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragment,  SettingsFragment.newInstance());

        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opinion:
                startActivity(new Intent(SettingsActivity.this,OpinionActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
