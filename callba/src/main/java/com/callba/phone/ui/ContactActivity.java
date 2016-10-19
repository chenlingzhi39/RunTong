package com.callba.phone.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.listener.InputWindowListener;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.InitiateSearch;
import com.callba.phone.util.RxBus;
import com.callba.phone.widget.IMMListenerRelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_contact2,
        toolbarTitle = R.string.list,
        menuId = R.menu.menu_contact
)
public class ContactActivity extends BaseActivity {
    @InjectView(R.id.layout_tab)
    TabLayout layoutTab;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
    @InjectView(R.id.view_search)
    IMMListenerRelativeLayout viewSearch;
    @InjectView(R.id.image_search_back)
    ImageView imageSearchBack;
    @InjectView(R.id.edit_text_search)
    EditText editTextSearch;
    @InjectView(R.id.clearSearch)
    ImageView clearSearch;
    @InjectView(R.id.linearLayout_search)
    LinearLayout linearLayoutSearch;
    @InjectView(R.id.line_divider)
    View lineDivider;
    @InjectView(R.id.card_search)
    CardView cardSearch;
    private InitiateSearch initiateSearch;
    private int index;

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
                index = position;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Log.i("active", imm.isActive() + "");
                if (imm.isActive())
                    imm.hideSoftInputFromWindow(viewpager.getWindowToken(), 0);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        InitiateSearch();
        HandleSearch();
    }
    private void InitiateSearch() {
        viewSearch.setListener(new InputWindowListener() {
            @Override
            public void show() {

            }

            @Override
            public void hide() {
                Log.i("input", "hide");
                if (cardSearch.getVisibility() == View.VISIBLE)
                    InitiateSearch.handleToolBar1(ContactActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RxBus.get().post("search_contact",s);
                if (editTextSearch.getText().toString().length() == 0) {
                    clearSearch.setVisibility(View.GONE);
                } else {
                    clearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
                ((InputMethodManager) ContactActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }
    private void HandleSearch() {
        imageSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "back");
                initiateSearch.handleToolBar(ContactActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
            }
        });
        editTextSearch.requestFocus();
    }


    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"本地", "Call吧"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LocalContactFragment2.newInstance();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            Log.i(this.getClass().getName(), "light");
            if (Build.MANUFACTURER.equals("Xiaomi"))
                ActivityUtil.MIUISetStatusBarLightMode(getWindow(), true);
            if (Build.MANUFACTURER.equals("Meizu"))
                ActivityUtil.FlymeSetStatusBarLightMode(getWindow(), true);
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (index == 0) {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.dir/person");
                    intent.setType("vnd.android.cursor.dir/contact");
                    intent.setType("vnd.android.cursor.dir/raw_contact");
                    if (!isIntentAvailable(this, intent)) {
                        return true;
                    } else {
                        startActivity(intent);
                    }
                } else {
                    startActivity(new Intent(ContactActivity.this, AddContactActivity.class));
                }
                break;
            case R.id.search:
                initiateSearch.handleToolBar(ContactActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }

}
