package com.callba.phone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.ui.adapter.CouponAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Coupon;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/30.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.discount,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.discount
)
public class CouponActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.coupon_list)
    RecyclerView couponList;
    CouponAdapter couponAdapter;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.retry)
    TextView retry;
    @BindView(R.id.hint)
    TextView hint;
    Gson gson;
    ArrayList<Coupon> coupons;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        couponAdapter = new CouponAdapter(this);
        gson = new Gson();
        getCoupon();
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeResources(R.color.orange);
    }

    @Override
    public void onRefresh() {
        getCoupon();
    }

    public void getCoupon() {
        OkHttpUtils.post().url(Interfaces.COUPON).addParams("loginName", getUsername()).addParams("loginPwd", getPassword())
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                refresh.setRefreshing(false);}

            @Override
            public void onBefore(Request request, int id) {
                retry.setVisibility(View.GONE);
                refresh.setRefreshing(true);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                retry.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
                Logger.i("coupon_result", response);
                try {
                String[] result = response.split("\\|");
                if (result[0].equals("0")) {
                    coupons = gson.fromJson(result[1], new TypeToken<ArrayList<Coupon>>() {
                    }.getType());
                    couponAdapter.clear();
                    couponAdapter.addAll(coupons);
                    couponList.setAdapter(couponAdapter);
                } else {
                    hint.setText(result[1]);
                    hint.setVisibility(View.VISIBLE);
                    couponAdapter.clear();
                }}catch(Exception e){
                toast(R.string.getserverdata_exception);
            }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) getCoupon();
    }
}
