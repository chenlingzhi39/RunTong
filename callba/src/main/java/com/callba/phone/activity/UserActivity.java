package com.callba.phone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.SharedPreferenceUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_user,
        toolbarTitle = R.string.center
)
public class UserActivity extends BaseActivity {
    @InjectView(R.id.logout)
    RelativeLayout logout;
    @InjectView(R.id.change_info)
    RelativeLayout change_info;
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.retrieve)
    RelativeLayout retrieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        number.setText(CalldaGlobalConfig.getInstance().getUsername());
    }

    @OnClick(R.id.logout)
    public void logout() {
        CalldaGlobalConfig.getInstance().setUsername("");
        CalldaGlobalConfig.getInstance().setPassword("");
        CalldaGlobalConfig.getInstance().setIvPath("");
        LoginController.getInstance().setUserLoginState(false);
        SharedPreferenceUtil.getInstance(this).putString(Constant.LOGIN_PASSWORD, "", true);
        Intent intent0 = new Intent("location");
        intent0.putExtra("action", "logout");
        sendBroadcast(intent0);
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        for (Activity activity : MyApplication.activities) {
            activity.finish();
        }
        startActivity(intent);
    }

    @OnClick(R.id.change_info)
    public void change_info() {
        Intent intent = new Intent(UserActivity.this, ChangeInfoActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.change_password)
    public void change_password() {
        Intent intent = new Intent(UserActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.retrieve)
    public void retrieve(){
        Intent intent = new Intent(UserActivity.this, RetrievePasswordActivity.class);
        startActivity(intent);
    }
    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
