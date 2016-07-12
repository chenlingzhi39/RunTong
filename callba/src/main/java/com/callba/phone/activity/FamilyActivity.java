package com.callba.phone.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;

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
        Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(avatar);
        name.setText(CalldaGlobalConfig.getInstance().getUsername());
        myCommission.setText("我的佣金:"+CalldaGlobalConfig.getInstance().getCommission());
    }

    @OnClick({R.id.name, R.id.my_commission, R.id.get_commission, R.id.btn_commission, R.id.order, R.id.commission_detail, R.id.team, R.id.type, R.id.qr_code})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.name:
                break;
            case R.id.my_commission:
                break;
            case R.id.get_commission:
                break;
            case R.id.btn_commission:
                break;
            case R.id.order:
                break;
            case R.id.commission_detail:
                break;
            case R.id.team:
                break;
            case R.id.type:
                break;
            case R.id.qr_code:
                break;
        }
    }
}
