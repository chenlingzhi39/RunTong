package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.CouponAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Coupon;
import com.callba.phone.bean.Team;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
public class CouponActivity extends BaseActivity {
    @InjectView(R.id.coupon_list)
    RecyclerView couponList;
    CouponAdapter couponAdapter;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.retry)
    TextView retry;
    @InjectView(R.id.hint)
    TextView hint;
    Gson gson;
    ArrayList<Coupon> coupons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        couponAdapter = new CouponAdapter(this);
        gson=new Gson();
    }
   public void getCoupon(){
       OkHttpUtils.post().url(Interfaces.COUPON).addParams("loginName", getUsername()).addParams("loginPwd", getPassword())
               .build().execute(new StringCallback() {
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
               Logger.i("coupon_result", response);
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
               }
           }
       });
   }
    @Override
    protected void onResume() {
        super.onResume();
        getCoupon();
    }
}
