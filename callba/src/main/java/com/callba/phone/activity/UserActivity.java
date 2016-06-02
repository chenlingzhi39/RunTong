package com.callba.phone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @InjectView(R.id.account)
    RelativeLayout account;
    @InjectView(R.id.change_password)
    RelativeLayout changePassword;
    @InjectView(R.id.user_head)
    CircleImageView circleImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        number.setText(CalldaGlobalConfig.getInstance().getUsername());
        if(!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(circleImageView);
    }

    @Override
    protected void onResume() {
        if(!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(circleImageView);
        super.onResume();
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @OnClick({R.id.account, R.id.change_info, R.id.change_password, R.id.retrieve, R.id.logout})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.account:
                intent=new Intent(UserActivity.this,AccountActivity.class);
                startActivity(intent);
                break;
            case R.id.change_info:
                intent = new Intent(UserActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.change_password:
                intent = new Intent(UserActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.retrieve:
                intent = new Intent(UserActivity.this, RetrievePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                EMClient.getInstance().login(SharedPreferenceUtil.getInstance(this).getString(Constant.LOGIN_USERNAME), SharedPreferenceUtil.getInstance(this).getString(Constant.LOGIN_PASSWORD), new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {

                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        Log.d("main", "登录聊天服务器成功！");


                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.d("main", "登录聊天服务器失败！");
                    }
                });
                CalldaGlobalConfig.getInstance().setUsername("");
                CalldaGlobalConfig.getInstance().setPassword("");
                CalldaGlobalConfig.getInstance().setIvPath("");
                LoginController.getInstance().setUserLoginState(false);
                SharedPreferenceUtil.getInstance(this).putString(Constant.LOGIN_PASSWORD, "", true);
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                intent = new Intent();
                intent.setClass(this, LoginActivity.class);
                for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }
                startActivity(intent);
                break;
        }
    }
}
