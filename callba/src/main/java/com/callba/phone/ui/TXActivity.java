package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/20.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tx,
        toolbarTitle = R.string.get_commission,
        navigationId = R.drawable.press_back
)
public class TXActivity extends BaseActivity {

    @BindView(R.id.commission)
    EditText commission;
    @BindView(R.id.account)
    EditText account;
    @BindView(R.id.name)
    EditText name;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.ok)
    public void onClick() {
        if(commission.getText().toString().equals("")){
            toast("佣金数量不能为空");
            return;
        }
        if(commission.getText().toString().equals("0"))
            return;
        if(Integer.parseInt(commission.getText().toString())%100!=0)
        {toast("佣金数量必须为100的整数倍");
            return;}
        if(account.getText().toString().equals(""))
        {toast("账户不能为空");
        return;}
        if(name.getText().toString().equals(""))
        {toast("姓名不能为空");
         return;
        }
        Logger.i("tx_url",Interfaces.TX+"?loginPwd="+getPassword()+"&loginName="+getUsername()+"&money="+commission.getText().toString()+"&payAccount="+account.getText().toString()+"&realName="+name.getText().toString());
        addRequestCall(OkHttpUtils.post().url(Interfaces.TX)
                .addParams("loginPwd", getPassword())
                .addParams("loginName", getUsername())
                .addParams("money",commission.getText().toString())
                .addParams("payAccount",account.getText().toString())
                .addParams("realName",name.getText().toString())
                .build())
                .execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                      progressDialog.dismiss();
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                    progressDialog=ProgressDialog.show(TXActivity.this,null,"正在提现");
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        toast(R.string.network_error);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                       try{String[] results=response.split("\\|");
                        if(results[0].equals("0")){
                            toast(results[1]);
                            UserManager.putCommission(TXActivity.this,Double.parseDouble(UserManager.getCommission(TXActivity.this))-Double.parseDouble(commission.getText().toString())+"");
                        }else toast(results[1]);
                       }catch(Exception e){
                           toast(R.string.getserverdata_exception);
                       }
                    }
                });
    }
}
