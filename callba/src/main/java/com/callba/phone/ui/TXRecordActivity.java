package com.callba.phone.ui;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.TxRecord;
import com.callba.phone.ui.adapter.TXRecordAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
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
 * Created by PC-20160514 on 2016/7/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.profit,
        toolbarTitle = R.string.tx_record,
        navigationId = R.drawable.press_back
)
public class TXRecordActivity extends BaseActivity {

    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.retry)
    TextView retry;
    @BindView(R.id.hint)
    TextView hint;
    private ArrayList<TxRecord> txRecords;
    private TXRecordAdapter txRecordAdapter;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        txRecordAdapter = new TXRecordAdapter(this);
        getTXRecords();
    }

    public void getTXRecords() {
        OkHttpUtils.post().url(Interfaces.TXRECORD)
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
                        try {
                            Logger.i("order_result", response);
                            String[] result = response.split("\\|");
                            if (result[0].equals("0")) {
                                txRecords = gson.fromJson(result[1], new TypeToken<ArrayList<TxRecord>>() {
                                }.getType());
                                txRecordAdapter.addAll(txRecords);
                                list.setAdapter(txRecordAdapter);
                                list.addItemDecoration(new DividerItemDecoration(
                                        TXRecordActivity.this, DividerItemDecoration.VERTICAL_LIST));
                            } else {
                                hint.setText(result[1]);
                                hint.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
    }

    @OnClick(R.id.retry)
    public void onClick() {
        getTXRecords();
    }
}
