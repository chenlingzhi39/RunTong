package com.callba.phone.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/6/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.gold,
        toolbarTitle = R.string.my_gold,
        navigationId = R.drawable.press_back
)
public class GoldActivity extends BaseActivity {
    @InjectView(R.id.gold)
    TextView gold;
    @InjectView(R.id.exchange)
    Button exchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        gold.setText(UserManager.getGold(this) + "");
    }

    @OnClick(R.id.exchange)
    public void onClick() {
        toast("暂未开放");
    }
}
