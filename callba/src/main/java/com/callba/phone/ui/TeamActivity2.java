package com.callba.phone.ui;

import android.content.Context;
import android.content.Intent;
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
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.Team;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/9/8.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.order,
        navigationId = R.drawable.press_back
)
public class TeamActivity2 extends BaseActivity {
    @BindView(R.id.layout_tab)
    TabLayout layoutTab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    ArrayList<ArrayList<Team>> teams;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.retry)
    TextView retry;
    @BindView(R.id.title)
    TextView title;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        gson = new Gson();
        getTeams();
    }

    public void getTeams() {
        title.setText(getIntent().getStringExtra("number")+"的团队");
        teams = new ArrayList<>();
        OkHttpUtils.post().url(Interfaces.GET_TEAM2)
                .addParams("loginPwd", getPassword())
                .addParams("loginName", getUsername())
                .addParams("phoneNumber", getIntent().getStringExtra("number"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        retry.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        retry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            Logger.i("order_result", response);
                            String[] result = response.split("\\|");
                            if (result[0].equals("0")) {
                                teams = gson.fromJson(result[1], new TypeToken<ArrayList<ArrayList<Team>>>() {
                                }.getType());
                                if(getSupportFragmentManager().getFragments()!=null)
                                getSupportFragmentManager().getFragments().clear();
                                viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), TeamActivity2.this));
                                layoutTab.setupWithViewPager(viewpager);
                                retry.setVisibility(View.GONE);
                            } else retry.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            toast(R.string.getserverdata_exception);
                        }
                    }

                });
    }

    @OnClick(R.id.retry)
    public void onClick() {
        getTeams();
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[]{"一级(" + teams.get(0).size() + ")", "二级(" + teams.get(1).size() + ")", "三级(" + teams.get(2).size() + ")"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            TeamFragment teamFragment = new TeamFragment();
            Bundle bundle = new Bundle();
            ArrayList list = new ArrayList();
            list.add(teams.get(position));
            bundle.putParcelableArrayList("list", list);
            teamFragment.setArguments(bundle);
            return teamFragment;
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
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        getTeams();
    }
}
