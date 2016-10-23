package com.callba.phone.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Order;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.order,
        toolbarTitle = R.string.all_order,
        navigationId = R.drawable.press_back
)
public class OrderActivity extends BaseActivity {
    @BindView(R.id.layout_tab)
    TabLayout layoutTab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    ArrayList<Order> orders;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.retry)
    TextView retry;
    @BindView(R.id.hint)
    TextView hint;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        gson = new Gson();
        orders = new ArrayList<>();
        getOrders();

    }

    public void getOrders() {
        OkHttpUtils.post().url(Interfaces.ORDER)
                .addParams("loginPwd", getPassword())
                .addParams("loginName", getUsername())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        retry.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAfter(int id) {
                        progressBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        retry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        retry.setVisibility(View.GONE);
                        Logger.i("order_result", response);
                        try {
                        String[] result = response.split("\\|");
                        if (result[0].equals("0")) {
                            orders = gson.fromJson(result[1], new TypeToken<ArrayList<Order>>() {
                            }.getType());
                            viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), OrderActivity.this));
                            layoutTab.setupWithViewPager(viewpager);
                        } else {
                            hint.setText(result[1]);
                            hint.setVisibility(View.VISIBLE);
                        }
                        }catch(Exception e){
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
    }

    @OnClick(R.id.retry)
    public void onClick() {
        getOrders();
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"未支付", "已支付"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            OrderFragment orderFragment = new OrderFragment();
            Bundle bundle = new Bundle();
            ArrayList list = new ArrayList();

            switch (position) {
                case 0:
                    list.add(filterList(0));
                    bundle.putParcelableArrayList("list", list);
                    orderFragment.setArguments(bundle);
                    return orderFragment;

                case 1:
                    list.add(new ArrayList<Order>());
                    bundle.putParcelableArrayList("list", list);
                    orderFragment.setArguments(bundle);
                    return orderFragment;
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

    public ArrayList<Order> filterList(int i) {
        ArrayList<Order> newOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getState()== i) newOrders.add(order);
        }
        return newOrders;
    }
}
