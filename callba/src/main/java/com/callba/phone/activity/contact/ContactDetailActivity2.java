package com.callba.phone.activity.contact;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.callba.R;
import com.callba.phone.activity.TestFragment;
import com.callba.phone.util.Logger;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/31.
 */
public class ContactDetailActivity2 extends AppCompatActivity {
    @InjectView(R.id.header)
    ImageView header;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tabs)
    TabLayout tabs;
    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @InjectView(R.id.appbar)
    AppBarLayout appbar;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
    @InjectView(R.id.main_content)
    CoordinatorLayout mainContent;
    private ContactMutliNumBean bean;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_detail2);
        ButterKnife.inject(this);
        bean = (ContactMutliNumBean) getIntent()
                .getSerializableExtra("contact");
        initToolbar();
        viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this));
        tabs.setupWithViewPager(viewpager);
        collapsingToolbar.setTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(bean.getDisplayName());


        }
    }
    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"通话记录", "详细信息"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TestFragment.newInstance();
                case 1:
                    ContactDetailFragment contactDetailFragment=new ContactDetailFragment();
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("contact",bean);
                    contactDetailFragment.setArguments(bundle);
                    return contactDetailFragment;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
