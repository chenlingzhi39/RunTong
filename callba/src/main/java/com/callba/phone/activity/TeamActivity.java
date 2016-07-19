package com.callba.phone.activity;

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
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Team;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.order,
        toolbarTitle = R.string.my_team,
        navigationId = R.drawable.press_back
)
public class TeamActivity extends BaseActivity {
    @InjectView(R.id.layout_tab)
    TabLayout layoutTab;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
    ArrayList<ArrayList<Team>> teams;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.retry)
    TextView retry;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        gson = new Gson();
        teams = new ArrayList<>();
        getTeams();

    }

    public void getTeams() {
        OkHttpUtils.post().url(Interfaces.TEAM)
                .addParams("loginPwd", getPassword())
                .addParams("loginName", getUsername())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        retry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Logger.i("order_result", response);
                        String[] result = response.split("\\|");
                        if (result[0].equals("0")) {
                            teams = gson.fromJson(result[1], new TypeToken<ArrayList<ArrayList<Team>>>() {
                            }.getType());
                            viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), TeamActivity.this));
                            layoutTab.setupWithViewPager(viewpager);
                            retry.setVisibility(View.GONE);
                        } else retry.setVisibility(View.VISIBLE);
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

            switch (position) {
                case 0:
                    list.add(teams.get(0));
                    bundle.putParcelableArrayList("list", list);
                    teamFragment.setArguments(bundle);
                    return teamFragment;

                case 1:
                    list.add(teams.get(1));
                    bundle.putParcelableArrayList("list", list);
                    teamFragment.setArguments(bundle);
                    return teamFragment;

                case 2:
                    list.add(teams.get(2));
                    bundle.putParcelableArrayList("list", list);
                    teamFragment.setArguments(bundle);
                    return teamFragment;
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
