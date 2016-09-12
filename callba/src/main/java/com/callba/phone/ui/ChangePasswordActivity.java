package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.Constant;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/5/24.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.change_password,
        toolbarTitle = R.string.change_password,
        navigationId = R.drawable.press_back
)
public class ChangePasswordActivity extends BaseActivity implements UserDao.PostListener {
    @InjectView(R.id.old_password)
    EditText oldPassword;
    @InjectView(R.id.new_password)
    EditText newPassword;
    @InjectView(R.id.ok)
    Button ok;
    @InjectView(R.id.confirm_new_password)
    EditText confirmNewPassword;
    private String old_password, new_password,comfirm_new_password;
    private ProgressDialog progressDialog;
    @Override
    public void start() {
      ok.setClickable(false);
    }

    @Override
    public void success(String msg) {
        toast(msg);
        ok.setClickable(true);
        UserManager.putOriginalPassword(this,new_password);
        try {
            String encryptPwd = DesUtil.encrypt(new_password,
                    UserManager.getToken(this));
            UserManager.putPassword(this,encryptPwd);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,getString(R.string.result_data_error),Toast.LENGTH_SHORT).show();
            UserManager.putOriginalPassword(this,new_password);
        }
        finish();
    }

    @Override
    public void failure(String msg) {
        toast(msg);
        ok.setClickable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        oldPassword.requestFocus();
        Timer timer = new Timer(); //设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { //弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(oldPassword.getWindowToken(), 0,InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300); //设置300毫秒的时长
    }

    @OnClick(R.id.ok)
    public void ok() {
        old_password = oldPassword.getText().toString();
        new_password = newPassword.getText().toString();
        comfirm_new_password=confirmNewPassword.getText().toString();
        if (old_password.equals("")) {
            toast(getString(R.string.input_old_password));
            return;
        }
            if (!old_password.equals(UserManager.getOriginalPassword(this))) {
                toast(getString(R.string.wrong_pwd));
                return;
            }
        if (old_password.equals(new_password)) {
            toast(getString(R.string.same_pwd));
            return;
        }
        if (new_password.equals("")) {
            toast(getString(R.string.input_new_password));
            return;
        }
        if(comfirm_new_password.equals("")){
            toast(getString(R.string.confirm_new_password));
            return;
        }
        if(!comfirm_new_password.equals(new_password)){
            toast(getString(R.string.confirm_new_password));
            return;
        }
        Pattern p = Pattern.compile("\\w{6,16}");
        Matcher m = p.matcher(new_password);
        if (!m.matches()) {
            toast(getString(R.string.pwd_type));
            return;
        }
        //userDao.changePassword(getUsername(), getPassword(),old_password,new_password);
        OkHttpUtils.post().url(Interfaces.Change_Pass)
                .addParams("loginName",getUsername())
                .addParams("loginPwd",getPassword())
                .addParams("oldPwd", old_password)
                .addParams("newPwd", new_password)
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if(e instanceof UnknownHostException)toast(R.string.conn_failed);
                else if(e instanceof SocketException){
                    if(!e.toString().contains("failed to connect to"))
                    {  toast("修改成功");
                    UserManager.putOriginalPassword(ChangePasswordActivity.this,new_password);
                    try {
                        String encryptPwd = DesUtil.encrypt(new_password,
                                UserManager.getToken(ChangePasswordActivity.this));
                        UserManager.putPassword(ChangePasswordActivity.this,encryptPwd);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        toast(R.string.result_data_error);
                    }
                    finish();}
                    else
                        toast(R.string.network_error);
                }
                else
                toast(R.string.network_error);
            }

            @Override
            public void onAfter(int id) {
               progressDialog.dismiss();
            }

            @Override
            public void onBefore(Request request, int id) {
               progressDialog=ProgressDialog.show(ChangePasswordActivity.this,"","正在修改密码");
            }

            @Override
            public void onResponse(String response, int id) {
                try{
                    Logger.i("change_result",response);
             String[] result=response.split("\\|");
                if(result[0].equals("0")){
                    toast(result[1]);
                    UserManager.putOriginalPassword(ChangePasswordActivity.this,new_password);
                    try {
                        String encryptPwd = DesUtil.encrypt(new_password,
                                UserManager.getToken(ChangePasswordActivity.this));
                        UserManager.putPassword(ChangePasswordActivity.this,encryptPwd);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast(R.string.result_data_error);
                    }
                    finish();
                }else{
                    toast(result[1]);
                }}catch (Exception e){
                    toast(R.string.network_error);
                }
            }
        });
    }
}
