package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by PC-20160514 on 2016/8/26.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.opinion,
        toolbarTitle = R.string.opinion,
        navigationId = R.drawable.press_back,
        menuId = R.menu.opinion
)
public class OpinionActivity extends BaseActivity {
    @BindView(R.id.opinion)
    TextView opinion;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        Timer timer = new Timer(); //设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { //弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(opinion.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300); //设置300毫秒的时长
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submit:
               if(opinion.getText().toString().equals(""))
               {toast("内容不能为空!");
                break;}
                else
                   addRequestCall(OkHttpUtils.post().url(Interfaces.OPINION)
                   .addParams("loginName",getUsername())
                   .addParams("loginPwd",getPassword())
                   .addParams("softType","android")
                   .addParams("content",opinion.getText().toString())
                   .build()).execute(new StringCallback() {
                       @Override
                       public void onAfter(int id) {
                           progressDialog.dismiss();
                       }

                       @Override
                       public void onBefore(Request request, int id) {
                          progressDialog=ProgressDialog.show(OpinionActivity.this,"","正在提交");
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
