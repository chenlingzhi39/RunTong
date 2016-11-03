package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Interfaces;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/8/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.remark,
        toolbarTitle = R.string.remark_info,
        navigationId = R.drawable.press_back,
        menuId = R.menu.remark
)
public class RemarkActivity extends BaseActivity {
    @BindView(R.id.remark)
    EditText remark;
    EaseUser user;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        user= EaseUserUtils.getUserInfo(getIntent().getStringExtra("username"));
        if(user.getRemark()!=null)
        {remark.setText(user.getRemark());
        remark.setSelection(user.getRemark().length());}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save:
                   if(user.getRemark()!=null)
                       if(user.getRemark().equals(remark.getText().toString()))
                           break;
                    addRequestCall(OkHttpUtils.post().url(Interfaces.UPDATE_REMARK)
                            .addParams("loginName",getUsername())
                            .addParams("loginPwd",getPassword())
                            .addParams("phoneNumber",user.getUsername().substring(0,11))
                            .addParams("remark",remark.getText().toString())
                            .build())
                            .execute(new StringCallback() {
                                @Override
                                public void onBefore(Request request, int id) {
                                   progressDialog=ProgressDialog.show(RemarkActivity.this,"","保存备注信息");
                                }

                                @Override
                                public void onAfter(int id) {
                                   progressDialog.dismiss();
                                }

                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    if(e instanceof UnknownHostException){
                                        toast(R.string.conn_failed);
                                    }else toast(R.string.network_error);
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                     try {
                                         String[] result=response.split("\\|");
                                         if(result[0].equals("0"))
                                         {  user.setRemark(remark.getText().toString());
                                             EaseCommonUtils.setUserInitialLetter(user);
                                             DemoHelper.getInstance().saveContact(user);
                                             Intent intent=new Intent();
                                             intent.putExtra("remark",remark.getText().toString());
                                            setResult(RESULT_OK,intent);
                                         }
                                         toast(result[1]);
                                     }catch (Exception e){
                                         toast(R.string.getserverdata_exception);
                                     }
                                }
                            });


                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
