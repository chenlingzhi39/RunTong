package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/7/7.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.family,
        toolbarTitle = R.string.family,
        navigationId = R.drawable.press_back
)
public class FamilyActivity extends BaseActivity {

    @InjectView(R.id.avatar)
    CircleImageView avatar;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.my_commission)
    TextView myCommission;
    @InjectView(R.id.get_commission)
    Button getCommission;
    @InjectView(R.id.btn_commission)
    TextView btnCommission;
    @InjectView(R.id.order)
    TextView order;
    @InjectView(R.id.commission_detail)
    TextView commissionDetail;
    @InjectView(R.id.team)
    TextView team;
    @InjectView(R.id.type)
    TextView type;
    @InjectView(R.id.qr_code)
    TextView qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        Glide.with(this).load(UserManager.getUserAvatar(this)).into(avatar);
        name.setText(getUsername());
    }

    @Override
    protected void onResume() {
        myCommission.setText("我的佣金:"+ UserManager.getCommission(this));
        super.onResume();
    }

    @OnClick({R.id.name, R.id.my_commission, R.id.get_commission, R.id.btn_commission, R.id.order, R.id.commission_detail, R.id.team, R.id.type, R.id.qr_code})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.name:
                break;
            case R.id.my_commission:

                break;
            case R.id.get_commission:
                startActivity(new Intent(this,TXActivity.class));
                break;
            case R.id.btn_commission:
                startActivity(new Intent(this,TXRecordActivity.class));
                break;
            case R.id.order:
                startActivity(new Intent(this,OrderActivity.class));
                break;
            case R.id.commission_detail:
                startActivity(new Intent(this,ProfitActivity.class));
                break;
            case R.id.team:
                startActivity(new Intent(this,TeamActivity.class));
                break;
            case R.id.type:
                startActivity(new Intent(this,ATypeActivity.class));
                break;
            case R.id.qr_code:
                startActivity(new Intent(this,ImageQRActivity.class));
                break;
        }
    }
}
