package com.callba.phone.activity.recharge;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.TestFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Coupon;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.recharge2,
        toolbarTitle = R.string.flow,
        navigationId = R.drawable.press_back
)
public class FlowActivity extends BaseActivity {

    @InjectView(R.id.layout_tab)
    TabLayout layoutTab;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
    private Coupon coupon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        coupon=(Coupon) getIntent().getSerializableExtra("coupon");
        viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this));
        layoutTab.setupWithViewPager(viewpager);
        viewpager.setCurrentItem(getIntent().getIntExtra("index",0));
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("position", position + "");
                if (position == 1) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    Log.i("active", imm.isActive() + "");
                    if (imm.isActive())
                        imm.hideSoftInputFromWindow(viewpager.getWindowToken(), 0);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"卡号充值", "流量套餐"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return NumberFragment2.newInstance();

                case 1:
                    StraightFragment2 straightFragment2=new StraightFragment2();
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("coupon",coupon);
                    straightFragment2.setArguments(bundle);
                    return straightFragment2;

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
}
