package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CleanableEditText;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Request;

@ActivityFragmentInject(
        contentViewId = R.layout.login,
        toolbarTitle = R.string.login,
        menuId = R.menu.menu_login,
        navigationId = R.drawable.press_cancel
)
public class LoginActivity extends BaseActivity implements OnClickListener {
    private Button bn_login, bn_retrievePass;
    private EditText et_password;
    private CleanableEditText et_username;
    private ProgressDialog progressDialog;

    private String username; // 登录的用户名
    private String password; // 密码
    private SharedPreferenceUtil mPreferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bn_login = (Button) this.findViewById(R.id.bn_login_login);
        bn_retrievePass = (Button) this
                .findViewById(R.id.bn_login_retrievePass);

        bn_login.setOnClickListener(this);
        bn_retrievePass.setOnClickListener(this);

        et_username = (CleanableEditText) this.findViewById(R.id.et_login_name);
        et_password = (EditText) this.findViewById(R.id.et_login_password);
        mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        if (getIntent().getStringExtra("number") != null) {
            et_username.setText(getIntent().getStringExtra("number"));
            et_password.setText(getIntent().getStringExtra("password"));
        }
        username = UserManager.getUsername(this);
        if (!"".equals(username)) {
            et_username.setText(username);
        }
        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(bn_login.getWindowToken(), 0);
                    login();
                    mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, false, true);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_login_login:
                InputMethodManager imm = (InputMethodManager) this
                        .getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bn_login.getWindowToken(), 0);

                login();
                mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, false, true);

                break;

            case R.id.bn_login_retrievePass:
                // 找回密码
                Log.i("login", "receivePass");
                Intent intent_pass = new Intent(this,
                        RetrievePasswordActivity.class);
                startActivity(intent_pass);

                break;
            default:
                break;
        }
    }

    /**
     * 登录
     */
    private void login() {
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();

        //校验用户名是否为空
        if (TextUtils.isEmpty(username)) {
            /*CalldaToast calldaToast = new CalldaToast();
            calldaToast.showToast(getApplicationContext(), R.string.input_username);*/
            toast(getString(R.string.input_username));
            return;
        }

        //校验密码是否为空
        if (TextUtils.isEmpty(password)) {
		/*	CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_password);*/
            toast(getString(R.string.input_password));
            return;
        }

        // 加密，生成loginSign
        String source = username + "," + password;
        String sign = null;
        try {
            sign = DesUtil.encrypt(source, UserManager.getSecretKey(this));
        } catch (Exception e) {
            e.printStackTrace();
			
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.key_exception);*/
            toast(getString(R.string.key_exception));
            return;
        }


        OkHttpUtils.post().url(Interfaces.Login)
                .addParams("loginSign", sign)
                .addParams("loginType", "1")
                .addParams("softType", "android")
                .addParams("callType", "all")
        .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(LoginActivity.this, null,
                        getString(R.string.logining));
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                if(e instanceof ConnectTimeoutException){
                    toast(R.string.conn_timeout);
                }
                else if(e instanceof SocketTimeoutException){
                    toast(R.string.socket_timeout);
                }else if(e instanceof UnknownHostException){
                    toast(R.string.conn_failed);
                }else{
                    e.printStackTrace();
                    toast(R.string.network_error);}
            }

            @Override
            public void onResponse(String response, int id) {
                Logger.i("login_result",response);
                String[] resultInfo=response.split("\\|");
                if(resultInfo[0].equals("0"))
                {//处理登录成功返回信息
                    LoginController.getInstance().setUserLoginState(true);
                LoginController.parseLoginSuccessResult(LoginActivity.this, username, password, resultInfo);
                //转到主页面
                gotoMainActivity();}
                else toast(resultInfo[1]);
            }
        });
    }

    /**
     * 跳转到主页面
     */
    private void gotoMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainTabActivity.class);
        this.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GuideActivity la = (GuideActivity) ActivityUtil.getActivityByName("GuideActivity");
            if (la == null) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, GuideActivity.class);
                startActivity(intent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        ArrayList list = new ArrayList();
        list.add(GlobalConfig.getInstance().getContactBeans());
        outState.putParcelableArrayList("contact", list);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        GlobalConfig.getInstance().setContactBeans((ArrayList<ContactPersonEntity>) savedInstanceState.getParcelableArrayList("contact").get(0));
    }
}
