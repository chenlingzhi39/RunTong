package com.callba.phone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;

import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/6/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.balance,
        toolbarTitle = R.string.balance,
        navigationId = R.drawable.press_back
)
public class BalanceActivity extends BaseActivity {

    @InjectView(R.id.recharge)
    Button recharge;
    UserDao userDao;
    @InjectView(R.id.balance)
    TextView balance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        userDao = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
             balance.setText(msg);
            }

            @Override
            public void failure(String msg) {

            }
        });
        userDao.getBalance(getUsername(), getPassword());
    }

    @OnClick(R.id.recharge)
    public void onClick() {
        startActivity(new Intent(BalanceActivity.this, RechargeActivity2.class));
    }
}
