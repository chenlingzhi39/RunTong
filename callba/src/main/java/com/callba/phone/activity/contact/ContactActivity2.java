package com.callba.phone.activity.contact;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.SystemBarTintManager;
import com.callba.phone.activity.HomeActivity;
import com.callba.phone.activity.MainCallActivity;
import com.callba.phone.activity.MessageActivity;
import com.callba.phone.activity.UserActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.util.ActivityUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_contact2,
        menuId=R.menu.menu_contact
)
public class ContactActivity2 extends BaseActivity {


    @InjectView(R.id.layout_tab)
    TabLayout layoutTab;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this));
        layoutTab.setupWithViewPager(viewpager);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
             switch (position){
                 case 0:

                     break;
                 case 1:
                     break;
             }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"本地", "环信"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LocalContactFragment.newInstance();

                case 1:
                    return WebContactFragment.newInstance();

                default:
                    return null;

            }

        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    @Override
    protected void onResume() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                Log.i(this.getClass().getName(),"light");
                if(Build.MANUFACTURER.equals("Xiaomi"))
                    ActivityUtil.MIUISetStatusBarLightMode(getWindow(),true);
                if(Build.MANUFACTURER.equals("Meizu"))
                    ActivityUtil.FlymeSetStatusBarLightMode(getWindow(),true);
            }
        super.onResume();
    }
}
