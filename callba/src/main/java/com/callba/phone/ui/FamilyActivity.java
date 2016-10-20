package com.callba.phone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/7/7.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.family,
        toolbarTitle = R.string.family,
        navigationId = R.drawable.press_back
)
public class FamilyActivity extends BaseActivity {

    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.my_commission)
    TextView myCommission;
    @BindView(R.id.get_commission)
    Button getCommission;
    @BindView(R.id.btn_commission)
    TextView btnCommission;
    @BindView(R.id.order)
    TextView order;
    @BindView(R.id.commission_detail)
    TextView commissionDetail;
    @BindView(R.id.team)
    TextView team;
    @BindView(R.id.type)
    TextView type;
    @BindView(R.id.qr_code)
    TextView qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        if (!UserManager.getUserAvatar(this).equals(""))
            Glide.with(this).load(UserManager.getUserAvatar(this)).into(avatar);
        name.setText(getUsername());
        myCommission.setText(UserManager.getCommission(this));
        OkHttpUtils.post().url(Interfaces.USER_INFO)
                .addParams("loginName",getUsername())
                .addParams("loginPwd",getPassword())
                .addParams("phoneNumber",getUsername())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            Logger.i("info_result",response);
                            String[] result = response.split("\\|");
                            if(result[0].equals("0"))
                            {JSONObject jsonObject=new JSONObject(result[1]);
                            UserManager.putCommission(FamilyActivity.this,jsonObject.getString("profit"));
                                myCommission.setText(UserManager.getCommission(FamilyActivity.this));
                            }
                        }
                    catch (Exception e){}
                    }
                });
    }

    @Override
    protected void onResume() {
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
                startActivity(new Intent(this, TXActivity.class));
                break;
            case R.id.btn_commission:
                startActivity(new Intent(this, TXRecordActivity.class));
                break;
            case R.id.order:
                startActivity(new Intent(this, OrderActivity.class));
                break;
            case R.id.commission_detail:
                startActivity(new Intent(this, ProfitActivity.class));
                break;
            case R.id.team:
                startActivity(new Intent(this, TeamActivity.class));
                break;
            case R.id.type:
                startActivity(new Intent(this, ATypeActivity.class));
                break;
            case R.id.qr_code:
                startActivity(new Intent(this, ImageQRActivity.class));
                break;
        }
    }
}
