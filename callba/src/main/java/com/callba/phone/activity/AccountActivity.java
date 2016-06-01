package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.more.QueryCalllogActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/6/1.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.account,
        toolbarTitle = R.string.my_number,
        navigationId = R.drawable.press_back
)
public class AccountActivity extends BaseActivity {
    @InjectView(R.id.head)
    CircleImageView head;
    @InjectView(R.id.account)
    TextView account;
    @InjectView(R.id.meal)
    TextView meal;
    @InjectView(R.id.work_date)
    TextView workDate;
    @InjectView(R.id.balance)
    Button balance;
    UserDao userDao, userDao1;
    @InjectView(R.id.calllog_search)
    RelativeLayout calllogSearch;

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        account.setHint(CalldaGlobalConfig.getInstance().getUsername());
        Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(head);
        userDao = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                if (msg == null) {
                    meal.setHint("无套餐");
                    workDate.setHint("无");
                }
            }

            @Override
            public void failure(String msg) {
                toast(msg);
            }
        });
        userDao1 = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                balance.setText(msg + "元");
            }

            @Override
            public void failure(String msg) {

            }
        });
        userDao.getSuits(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());
        userDao1.getBalance(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());

    }

    @OnClick(R.id.calllog_search)
    public void onClick() {
        Intent intent=new Intent(AccountActivity.this, QueryCalllogActivity.class);
        startActivity(intent);
    }
}
