package com.callba.phone.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.SharedPreferenceUtil;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

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
    private UserDao userDao;
    private String old_password, new_password,comfirm_new_password;

    @Override
    public void start() {
      ok.setClickable(false);
    }

    @Override
    public void success(String msg) {
        toast(msg);
        ok.setClickable(true);
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
        userDao=new UserDao(this,this);
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
            if (!old_password.equals(SharedPreferenceUtil.getInstance(this).getString(Constant.LOGIN_PASSWORD))) {
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
        userDao.changePassword(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),old_password,new_password);
    }
}
