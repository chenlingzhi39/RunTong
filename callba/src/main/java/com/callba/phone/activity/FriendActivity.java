package com.callba.phone.activity;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.view.AlwaysMarqueeTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.friend,
        toolbarTitle = R.string.friend,
        navigationId = R.drawable.press_back
)
public class FriendActivity extends BaseActivity implements UserDao.PostListener {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mToolbarContainer)
    AppBarLayout mToolbarContainer;
    @InjectView(R.id.location)
    AlwaysMarqueeTextView location;
    private UserDao userDao;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        location.setText(CalldaGlobalConfig.getInstance().getAddress());
        userDao=new UserDao(this,this);
        userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),CalldaGlobalConfig.getInstance().getLatitude(),CalldaGlobalConfig.getInstance().getLongitude(),100000);
    }

    @Override
    public void failure(String msg) {
        toast(msg);
    }

    @Override
    public void start() {

    }
    @Override
    public void success(String msg) {
        toast(msg);
    }

    @Override
    public void init() {

    }



    @Override
    public void refresh(Object... params) {

    }
}
