package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SmsTools;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;


@ActivityFragmentInject(
        contentViewId = R.layout.manual_register,
        toolbarTitle = R.string.register,
        menuId = R.menu.menu_register,
        navigationId = R.drawable.press_cancel
)
public class RegisterActivity extends BaseActivity implements OnClickListener {
    private Button bn_back, bn_ok_register;
    private Button bn_register, bn_login;
    private ProgressDialog progressDialog;
    private EditText et_account, et_verification, et_password, et_yzm;
    private CheckBox hide;
    private Button send_yzm;
    private InputMethodManager imm;
    private String username;    //获取验证码的手机号
    private String password;
    private String code;
    private String language = "";
    UserDao userDao;
    String key;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    ContentObserver c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        init();
        c = new ContentObserver(han) {
            @Override
            public void onChange(boolean selfChange) {
                // TODO Auto-generated method stub
                super.onChange(selfChange);
                Logger.i("sms_change", true + "");
                han.sendEmptyMessage(0);
            }
        };
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, c);
    }

    Handler han = new Handler() {
        @SuppressWarnings("deprecation")
        public void handleMessage(android.os.Message msg) {
            String codestr = null;
            try {
                codestr = SmsTools.getsmsyzm(RegisterActivity.this);
                et_yzm.setText(codestr);
            } catch (Exception e) {
                Log.e("yung", "验证码提取失败:" + codestr);
            }
        }

        ;
    };

    @OnClick(R.id.private_clause)
    public void onClick() {
        Intent intent = new Intent(RegisterActivity.this, ClauseActivity.class);
        startActivity(intent);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RegisterActivity> mActivity;
        static TimeCount time;

        public MyHandler(RegisterActivity activity) {
            mActivity = new WeakReference<RegisterActivity>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity activity = mActivity.get();
            if (activity != null) {
                // ...
                switch (msg.what) {
                    case Interfaces.GET_KEY_START:
                        activity.bn_register.setClickable(false);
                        break;
                    case Interfaces.GET_KEY_SUCCESS:
                        activity.key = (String) msg.obj;
                        break;
                    case Interfaces.GET_KEY_FAILURE:
                        activity.bn_register.setClickable(true);
                        activity.toast((String) msg.obj);
                        break;
                    case Interfaces.GET_CODE_START:
                        activity.toast("发送验证码请求");
                        break;
                    case Interfaces.GET_CODE_SUCCESS:
                        //activity.et_yzm.setText((String)msg.obj);
                        activity.toast((String) msg.obj);
                        time = new TimeCount(60000, 1000);
                        time.start();
                        activity.bn_register.setClickable(true);
                        break;
                    case Interfaces.GET_CODE_FAILURE:
                        activity.toast((String) msg.obj);
                        activity.bn_register.setClickable(true);
                        break;

                }
            }
        }

        class TimeCount extends CountDownTimer {
            RegisterActivity activity;

            public TimeCount(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
                activity = mActivity.get();
            }

            @Override
            public void onFinish() {// 计时完毕
                activity.send_yzm.setBackgroundColor(activity.getResources().getColor(R.color.orange));
                activity.send_yzm.setText(activity.getString(R.string.send_yzm));
                activity.send_yzm.setClickable(true);
            }

            @Override
            public void onTick(long millisUntilFinished) {// 计时过程
                activity.send_yzm.setClickable(false);//防止重复点击
                activity.send_yzm.setBackgroundColor(activity.getResources().getColor(R.color.light_black));
                activity.send_yzm.setText(millisUntilFinished / 1000 + "秒后重新发送");
            }
        }
    }


    private final MyHandler mHandler = new MyHandler(this);


    public void init() {
        userDao = new UserDao(this, mHandler);
        Locale locale = getResources().getConfiguration().locale;
        language = locale.getCountry();
        bn_back = (Button) this.findViewById(R.id.bn_mre_back);
        bn_ok_register = (Button) this.findViewById(R.id.bn_mre_yijianzhuce);
        bn_back.setOnClickListener(this);
        bn_ok_register.setOnClickListener(this);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        send_yzm = (Button) findViewById(R.id.send_yzm);
        send_yzm.setOnClickListener(this);
        bn_register = (Button) this.findViewById(R.id.bn_mre_register);
        bn_register.setOnClickListener(this);
        hide = (CheckBox) findViewById(R.id.hide);
        hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }

            }
        });
        et_account = (EditText) this.findViewById(R.id.et_mre_account);
    /*    if (!"".equals(GlobalConfig.getInstance().getUsername())) {
            et_account.setText(GlobalConfig.getInstance().getUsername());
        }*/
        et_verification = (EditText) this.findViewById(R.id.et_mre_yzm);
        et_password = (EditText) this.findViewById(R.id.et_mre_password);

        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        //注册广播
        //registerReceiver(mSMSBroadcastReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_yzm:

                final String searchNumber = et_account.getText().toString().trim();
                if (searchNumber.equals("")) {
                    toast("手机号不能为空!");
                    break;
                }
                if (searchNumber.length() > 10) {
                /*	String address = NumberAddressService.getAddress(
                            searchNumber, Constant.DB_PATH,
							RegisterActivity.this);
				if(!address.equals(""))
				{ */
                    userDao.getRegisterKey(searchNumber);
				/*}
					else
				toast("请输入正确的手机号!");*/
                } else
                    toast("请输入正确的手机号!");

                break;
            case R.id.bn_mre_back:
                this.finish();
                break;


            case R.id.bn_mre_register:
                //隐藏键盘
                imm.hideSoftInputFromWindow(bn_register.getWindowToken(), 0);

                username = et_account.getText().toString().trim();
                password = et_password.getText().toString().trim();
                code = et_yzm.getText().toString().trim();
                if (et_account.getText().toString().equals("")) {
                    toast("手机号不能为空!");
                    break;
                }
                if (et_password.getText().toString().equals("")) {
                    toast("密码不能为空!");
                    break;
                }
                if (code.equals("")) {
                    toast("验证码不能为空!");
                    break;
                }
                boolean isinputOK = verification(username, password, code);

                if (!isinputOK) return;
                if (username.length() > 10) {
			/*	String address = NumberAddressService.getAddress(
						username, Constant.DB_PATH,
						RegisterActivity.this);
				if(!address.equals(""))
				{*/
                    try {
                        sendRegisterRequest(username, password, DesUtil.encrypt(code, key));
                    } catch (Exception e) {
                        toast("请重新获取验证码");
                        e.printStackTrace();
                    }
				/*}else {toast("请输入正确的手机号!");
				break;
			    }*/
                } else {
                    toast("请输入正确的手机号!");
                    break;
                }

			
			/*progressDialog = new MyProgressDialog(this, getString(R.string.registering));
			progressDialog.show();*/


		    /*try {
				userDao.register(username,password,DesUtil.encrypt(code,key));
			}catch(Exception e){

		   }*/
                break;


            default:
                break;
        }
    }


    /**
     * 注册之前校验
     *
     * @param username
     * @param password
     * @param rePass
     * @return 校验通过返回 true 反之 false
     */
    private boolean verification(String username, String password, String verifiCode) {
        if ("".equals(username) || username.length() < 1) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);*/
            toast(getString(R.string.input_phonenum));
            return false;
        }
        if ("".equals(password) || password.length() < 1) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_pwd);*/
            toast(getString(R.string.input_pwd));
            return false;
        }
		/*if("".equals(verifiCode) || verifiCode.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_yzm);
			return false;
		}*/
        //验证密码是否规范 6~16位的字母、数字或下划线
        Pattern p = Pattern.compile("\\w{6,16}");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.pwd_type);*/
            toast(getString(R.string.pwd_type));
            return false;
        }
        return true;
    }

    /**
     * 发送注册任务
     *
     * @param username
     * @param password
     * @param verifiCode
     */
    private void sendRegisterRequest(final String username, final String password,
                                     String code) {
        OkHttpUtils.post().url(Interfaces.Register)
                .addParams("phoneNumber", username)
                .addParams("password", password)
                .addParams("code", code)
                .addParams("softType", "android")
                .addParams("countryCode", "86")
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                progressDialog.dismiss();
            }

            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(RegisterActivity.this, "", "正在注册");
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                if (e instanceof UnknownHostException) {
                    toast(R.string.conn_failed);
                } else {
                    toast(R.string.network_error);
                }
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    String[] content = response.split("\\|");
                    if ("0".equals(content[0])) {
                        toast(content[1]);
                        UserManager.putUsername(RegisterActivity.this,username);
                        UserManager.putOriginalPassword(RegisterActivity.this,password);
                        Intent intent = new Intent(RegisterActivity.this, MainTabActivity.class);
                        LoginController.getInstance().setUserLoginState(false);
					/*new Thread(new Runnable() {
						@Override
						public void run() {
							try{
								EMClient.getInstance().createAccount(username,password); }catch(HyphenateException e){
								e.printStackTrace();
							}
						}
					}).start();*/
                        startActivity(intent);
                    } else {
                        toast(content[1]);
                    }
                } catch (Exception e) {
                    toast(R.string.getserverdata_exception);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(c);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
