package com.callba.phone.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.ProfitAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Profit;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.profit,
        toolbarTitle = R.string.commission_detail,
        navigationId = R.drawable.press_back
)
public class ProfitActivity extends BaseActivity {

    @InjectView(R.id.list)
    RecyclerView list;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.hint)
    TextView hint;
    @InjectView(R.id.retry)
    TextView retry;
    private Gson gson = new Gson();
    private List<Profit> profits;
    private ProfitAdapter profitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        profitAdapter = new ProfitAdapter(this);
        getProfits();
    }
public void getProfits(){
    OkHttpUtils.post().url(Interfaces.PROFIT)
            .addParams("loginPwd", getPassword())
            .addParams("loginName", getUsername())
            .build()
            .execute(new StringCallback() {
                @Override
                public void onBefore(Request request, int id) {
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
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        profits = gson.fromJson(result[1], new TypeToken<ArrayList<Profit>>() {
                        }.getType());
                        profitAdapter.addAll(profits);
                        list.setAdapter(profitAdapter);
                        list.addItemDecoration(new DividerItemDecoration(
                                ProfitActivity.this, DividerItemDecoration.VERTICAL_LIST));
                    } else {
                        hint.setText(result[1]);
                        hint.setVisibility(View.VISIBLE);
                    }
                }
            });
}
    @OnClick(R.id.retry)
    public void onClick() {
        getProfits();
    }
}
