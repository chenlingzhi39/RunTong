package com.callba.phone.ui;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.view.MyProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Request;

@ActivityFragmentInject(
        contentViewId = R.layout.more_retrievepass,
        toolbarTitle = R.string.find_password,
        navigationId = R.drawable.press_back
)
public class RetrievePasswordActivity extends BaseActivity implements OnClickListener {

    private Button bn_submit;
    private EditText et_phoneNum;

    private String language = "";
    private Message message;
    private final MyHandler mHandler = new MyHandler(this);
    private TimeCount time;

    private static class MyHandler extends Handler {
        private final WeakReference<RetrievePasswordActivity> mActivity;
        private TimeCount time;

        public MyHandler(RetrievePasswordActivity activity) {
            mActivity = new WeakReference<RetrievePasswordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RetrievePasswordActivity activity = mActivity.get();
            if (activity != null) {
                // ...
                switch (msg.what) {
                    case Interfaces.GET_KEY_START:
                        activity.bn_submit.setClickable(false);
                        break;
                    case Interfaces.GET_KEY_FAILURE:
                        activity.toast((String) msg.obj);
                        activity.bn_submit.setClickable(true);
                        break;
                    case Interfaces.GET_KEY_SUCCESS:
                        //activity.toast((String)msg.obj);
                        break;
                    case Interfaces.GET_CODE_START:
                        activity.toast("发送短信请求");
                        break;
                    case Interfaces.GET_CODE_FAILURE:
                        activity.toast((String) msg.obj);
                        activity.bn_submit.setClickable(true);
                        break;
                    case Interfaces.GET_CODE_SUCCESS:
                        activity.toast((String) msg.obj);
                        time = new TimeCount(60000, 1000);
                        time.start();
                        break;

                }
            }
        }

        class TimeCount extends CountDownTimer {
            RetrievePasswordActivity activity;

            public TimeCount(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
                activity = mActivity.get();
            }

            @Override
            public void onFinish() {// 计时完毕
                activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.orange));
                activity.bn_submit.setText(activity.getString(R.string.send_yzm));
                activity.bn_submit.setClickable(true);
            }

            @Override
            public void onTick(long millisUntilFinished) {// 计时过程
                activity.bn_submit.setClickable(false);//防止重复点击
                activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.light_black));
                activity.bn_submit.setText(millisUntilFinished / 1000 + "秒后重新发送");
            }
        }

    }

    class TimeCount extends CountDownTimer {
        RetrievePasswordActivity activity;

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.orange));
            bn_submit.setText(activity.getString(R.string.send_yzm));
            bn_submit.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            bn_submit.setClickable(false);//防止重复点击
            bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.light_black));
            bn_submit.setText(millisUntilFinished / 1000 + "秒后重新发送");
        }
    }

    public void getMessage(int code) {
        message = mHandler.obtainMessage();
        message.what = code;
    }

    public void init() {
        Locale locale = getResources().getConfiguration().locale;
        language = locale.getCountry();
        bn_submit = (Button) this.findViewById(R.id.bn_retrieve_pass);
        bn_submit.setOnClickListener(this);
        et_phoneNum = (EditText) this.findViewById(R.id.et_retrpass_phone);
        et_phoneNum.requestFocus();
        et_phoneNum.setText(getUsername());
        Timer timer = new Timer(); //设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { //弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(et_phoneNum.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300); //设置300毫秒的时长
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_retrieve_pass:
                InputMethodManager imm = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bn_submit.getWindowToken(), 0);
                String num = et_phoneNum.getText().toString().trim();
                if (num.equals("")) {
                    toast("手机号不能为空!");
                    return;
                }
                if (num.length() > 10) {
            /*	String address = NumberAddressService.getAddress(
						num, Constant.DB_PATH,
						RetrievePasswordActivity.this);
				if(!address.equals(""))
				{*/
                    //userDao.getFindKey(num);
                    findKey(num);
				/*}else {toast("请输入正确的手机号!");
					return;
				}*/
                } else {
                    toast("请输入正确的手机号!");
                    return;
                }

                //getSMSCode();
                break;

            default:
                break;
        }
    }

    public void findKey(final String num) {
        OkHttpUtils.post().url(Interfaces.Send_SMS)
                .addParams("phoneNumber", num)
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                super.onAfter(id);
            }

            @Override
            public void onBefore(Request request, int id) {
                Log.i("url", Interfaces.Send_SMS);
                bn_submit.setClickable(false);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                showException(e);
                bn_submit.setClickable(true);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.i("send_success", response);
                try {
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        String phoneNumber2;
                        try {
                            phoneNumber2 = DesUtil.encrypt(num, result[1]);
                            Log.i("phoneNumber", phoneNumber2);
                            retrievePassword(phoneNumber2, result[2]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            bn_submit.setClickable(true);
                            toast(R.string.getserverdata_exception);
                        }
                    } else {
                        bn_submit.setClickable(true);
                        toast(result[1]);
                    }
                } catch (Exception e) {
                    bn_submit.setClickable(true);
                    toast(R.string.getserverdata_exception);
                }
            }
        });
    }

    public void retrievePassword(String phoneNumber, String sign) {
        OkHttpUtils.post().url(Interfaces.Retrieve_Pass)
                .addParams("phoneNumber", phoneNumber)
                .addParams("sign", sign)
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                Log.i("url", Interfaces.Retrieve_Pass);
                toast("发送短信请求");
            }

            @Override
            public void onAfter(int id) {

            }

            @Override
            public void onError(Call call, Exception e, int id) {
                showException(e);
                bn_submit.setClickable(true);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.i("get_success", response);
                try {
                    String[] result = response.split("\\|");
                    toast(result[1]);
                    if (result[0].equals("0")) {
                        time = new TimeCount(60000, 1000);
                        time.start();
                    } else {
                        bn_submit.setClickable(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    toast(R.string.getserverdata_exception);
                    bn_submit.setClickable(true);
                }
            }
        });

    }
}
