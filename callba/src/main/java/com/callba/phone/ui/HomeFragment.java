package com.callba.phone.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.HomeItem;
import com.callba.phone.ui.adapter.HomeAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.view.BannerLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/10/30.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_home,
        toolbarTitle = R.string.home
)
public class HomeFragment extends BaseFragment {
    @BindView(R.id.list)
    RecyclerView list;
    private ArrayList<HomeItem> homeItems;
    private HomeAdapter homeAdapter;
    private BannerLayout banner;
    private ArrayList<Integer> localImages = new ArrayList<>();
    private ArrayList<String> webImages = new ArrayList<>();
    private Gson gson=new Gson();
    private Observable<Boolean> mRefreshAdObservable;
    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.bind(this, fragmentRootView);
        mRefreshAdObservable= RxBus.get().register("refresh_ad", Boolean.class);
        mRefreshAdObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                getAd();
            }
        });
        homeItems = new ArrayList<>();
        homeItems.add(new HomeItem(getString(R.string.recharge), R.drawable.recharge));
        homeItems.add(new HomeItem(getString(R.string.flow), R.drawable.flow));
        homeItems.add(new HomeItem(getString(R.string.discount), R.drawable.discount));
        homeItems.add(new HomeItem(getString(R.string.family), R.drawable.family));
        homeItems.add(new HomeItem(getString(R.string.mall), R.drawable.mall));
        homeItems.add(new HomeItem(getString(R.string.service), R.drawable.service));
        homeItems.add(new HomeItem(getString(R.string.game), R.drawable.game));
        homeItems.add(new HomeItem(getString(R.string.sign_in), R.drawable.signin));
        homeAdapter = new HomeAdapter(getActivity());
        homeAdapter.addAll(homeItems);
        homeAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return getActivity().getLayoutInflater().inflate(R.layout.home_header, null);
            }

            @Override
            public void onBindView(View headerView) {
                banner = (BannerLayout) headerView.findViewById(R.id.banner);
                localImages.add(R.drawable.ad4);
                localImages.add(R.drawable.ad5);
                localImages.add(R.drawable.ad6);
                banner.setViewRes(localImages);
            }
        });
        homeAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent;
                switch (position){
                    case 0:
                        intent = new Intent(getActivity(), RechargeActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), FlowActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getActivity(), CouponActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(getActivity(), FamilyActivity.class));
                        break;
                    case 4:
                        toast("暂未开放");
                        break;
                    case 5:
                        toast("暂未开放");
                        break;
                    case 6:
                        toast("暂未开放");
                        break;
                    case 7:
                        intent = new Intent(getActivity(), SignInActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
        list.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        list.setAdapter(homeAdapter);
    }
    public void getAd(){
        OkHttpUtils.post().url(Interfaces.GET_ADVERTICEMENT1)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("ad_result", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        final ArrayList<Advertisement> list;
                        list = gson.fromJson(result[1], new TypeToken<ArrayList<Advertisement>>() {
                        }.getType());
                        webImages.clear();
                        for (Advertisement advertisement : list) {
                            webImages.add(advertisement.getImage());
                        }
                        banner.setViewUrls(webImages);
                        banner.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setData(Uri.parse(list.get(position).getAdurl()));
                                startActivity(intent1);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
