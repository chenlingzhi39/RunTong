package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;

import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.util.Interfaces;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/6/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.balance,
        toolbarTitle = R.string.balance,
        navigationId = R.drawable.press_back
)
public class BalanceActivity extends BaseActivity {

    @BindView(R.id.recharge)
    Button recharge;
    @BindView(R.id.balance)
    TextView balance;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        OkHttpUtils.post().url(Interfaces.Query_Balance)
                .addParams("loginName",getUsername())
                .addParams("loginPwd",getPassword())
                .addParams("softType","android")
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                progressDialog.dismiss();
            }

            @Override
            public void onBefore(Request request, int id) {
                progressDialog=ProgressDialog.show(BalanceActivity.this,"","正在查询余额");
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                if(e instanceof UnknownHostException)toast(R.string.conn_failed);
                else toast(R.string.network_error);
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    String[] result = response.split("\\|");
                    Log.i("get_balance", response);
                    if (result[0].equals("0"))
                        balance.setText(result[1]);
                    else toast(result[1]);
                } catch (Exception e) {
                   toast(R.string.network_error);
                }
            }
        });
    }

    @OnClick(R.id.recharge)
    public void onClick() {
        startActivity(new Intent(BalanceActivity.this, RechargeActivity.class));
    }
}
