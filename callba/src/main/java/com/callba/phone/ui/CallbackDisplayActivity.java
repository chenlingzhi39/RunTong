package com.callba.phone.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.DialAd;
import com.callba.phone.cfg.Constant;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.service.AutoAnswerReceiver;
import com.callba.phone.service.CalllogService;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SPUtils;
import com.callba.phone.util.TimeFormatUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;

@ActivityFragmentInject(
        contentViewId = R.layout.callback_display)
public class CallbackDisplayActivity extends BaseActivity {
    private String name;
    private String number;
    private TextView tv_name;
    private TextView tv_num;
    private TextView tv_status;
    private TextView count_down;
    private CalllogService calllogService;
    private FloatingActionButton cancel, voice;
    private MediaPlayer mp;
    private Gson gson;
    private DialAd dialAd;
    private ImageView background;
    private CircleImageView avatar;
    private boolean state = false;
    TimeCount time;
    private int currentCallbackTime = 0;
    private Exception callbackException;
    /**
     * 延迟关闭当前页面
     * @author zhw
     */
    private void delayFinish() {
        //呼叫失败，延迟关闭
        time = new TimeCount(10000, 1000);
        time.start();
        count_down.setVisibility(View.VISIBLE);
             /*   SimpleHandler.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);*/
    }
    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onFinish() {// 计时完毕
            finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            count_down.setText(millisUntilFinished / 1000 + "秒后自动关闭");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_status = (TextView) findViewById(R.id.tv_status);
        cancel = (FloatingActionButton) findViewById(R.id.cancel);
        voice = (FloatingActionButton) findViewById(R.id.voice);
        background = (ImageView) findViewById(R.id.iv_call_bg);
        avatar = (CircleImageView) findViewById(R.id.avatar);
        count_down = (TextView) findViewById(R.id.countdown);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if ((boolean) SPUtils.get(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, true)) {
            voice.setImageResource(R.drawable.ic_notifications_on_white_24dp);
        } else {
            voice.setImageResource(R.drawable.ic_notifications_off_white_24dp);
        }
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((boolean) SPUtils.get(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, true)) {
                    voice.setImageResource(R.drawable.ic_notifications_off_white_24dp);
                    SPUtils.put(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, false);
                    if (mp != null) if (mp.isPlaying()) mp.pause();
                } else {
                    voice.setImageResource(R.drawable.ic_notifications_on_white_24dp);
                    SPUtils.put(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, true);
                    if (mp != null) mp.start();
                    else if (state) playSound();
                }
            }
        });
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("name");
        number = bundle.getString("number");
        if (!TextUtils.isEmpty(bundle.getString("id"))) {
            Bitmap bitmap = ContactsManager.getAvatar(this, bundle.getString("id"), true);
            if (bitmap != null)
                avatar.setImageBitmap(bitmap);
        }
        if (TextUtils.isEmpty(name))
            tv_name.setText("未知");
        else
            tv_name.setText(name);
        tv_num.setText(number);
        // tv_status.setText(number);
        calllogService = new CalllogService(this, null);
        callback();
     /*   if(MyApplication.getInstance().isaBoolean()){
            callback();
        }else{
            tv_status.setText("拨打太频繁，请稍后重试");
            delayFinish();
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.TRANSPARENT);
        }
        gson = new Gson();
        OkHttpUtils.post().url(Interfaces.GET_ADVERTICEMENT4)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("ad_result", response);

                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        List<DialAd> dialAds = gson.fromJson(result[1], new TypeToken<ArrayList<DialAd>>() {
                        }.getType());
                        if (dialAds.size() > 0) {
                            dialAd = dialAds.get(0);
                            Glide.with(CallbackDisplayActivity.this).load(dialAd.getImage()).into(background);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 统计回拨失败数据
     *
     * @param errorMsg
     * @author zhw
     */
    private void countCallbackFailedData(String errorMsg) {
        String errorTime = TimeFormatUtil.formatTimeRange();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("errorMsg", errorMsg);
        paramsMap.put("errorTime", errorTime);
        paramsMap.put("from", getUsername());
        paramsMap.put("to", number);
        if(callbackException!=null)
        paramsMap.put("exception",callbackException.toString());
        MobclickAgent.onEvent(this, "callback_failed", paramsMap);
    }

    private void callback() {
        OkHttpUtils.post().url(Interfaces.DIAL_CALLBACK)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .addParams("caller", getUsername())
                .addParams("callee", number)
                .build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                callbackException=e;
                if (e instanceof UnknownHostException) {
                    tv_status.setText(R.string.conn_failed);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.conn_failed));
                    delayFinish();
                } else if (e instanceof SocketTimeoutException) {
                    if (!e.toString().contains("failed to connect to")) {
                        state = true;
                        //回拨成功，开启自动接听
                        AutoAnswerReceiver.answerPhone(CallbackDisplayActivity.this);
                        //calllogService.saveBackCallLog(name, number);
                        if ((boolean) SPUtils.get(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, true))
                            playSound();
                        tv_status.setText("请接听Call吧来电");
                        MobclickAgent.onEvent(CallbackDisplayActivity.this, "callback_success");
                    } else {
                        if (currentCallbackTime < 3) {
                            callback();
                            currentCallbackTime++;
                        } else
                        {  tv_status.setText(R.string.network_error);
                            //统计回拨失败数据
                            countCallbackFailedData(getString(R.string.callback_timeout));
                            delayFinish();}
                    }
                } else {
                    tv_status.setText(R.string.network_error);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.network_error));

                    delayFinish();
                }
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    String result =  response.replace("\n", "").replace("\r", "");
                    String[] content = result.split("\\|");
                    if ("0".equals(content[0])) {
                        state = true;
                        //回拨成功，开启自动接听
                        AutoAnswerReceiver.answerPhone(CallbackDisplayActivity.this);
                        //calllogService.saveBackCallLog(name, number);
                        if ((boolean) SPUtils.get(CallbackDisplayActivity.this, Constant.SETTINGS, Constant.Callback_Ring, true))
                            playSound();
                        MobclickAgent.onEvent(CallbackDisplayActivity.this, "callback_success");
                    } else {
                        //统计回拨失败数据
                        countCallbackFailedData(content[1]);
                        delayFinish();
                    }
                    tv_status.setText(content[1]);
                } catch (Exception e) {
                    callbackException=e;
                    tv_status.setText(R.string.getserverdata_exception);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.server_error));
                    delayFinish();
                }
            }
        });
     /*   new Thread() {
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("loginName", getUsername());
                params.put("loginPwd", getPassword());
                params.put("softType", "android");
                params.put("caller", getUsername());
                params.put("callee", number);
                //params.put("lan", lan);

                Message msg = mHandler.obtainMessage();

                try {
                    Logger.v("", params + "");
                    if (NetworkDetector.detect(CallbackDisplayActivity.this.getApplicationContext())) {
                        String result = HttpUtils.getDataFromHttpPost(
                                Interfaces.DIAL_CALLBACK, params);
                        msg.what = Task.TASK_SUCCESS;
                        msg.obj = result.replace("\n", "").replace("\r", "");
                    } else {
                        // 无网络连接
                        msg.what = Task.TASK_NETWORK_ERROR;
                    }
                } catch (ConnectTimeoutException cte) {
                    cte.printStackTrace();
                    msg.what = Task.TASK_TIMEOUT;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    msg.what = Task.TASK_UNKNOWN_HOST;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = Task.TASK_FAILED;
                } finally {
                    mHandler.sendMessage(msg);
                }
            }
        }.start();*/

    }

    @Override
    protected void onResume() {
        setSendNotification(false);
        super.onResume();
    }


    /**
     * 播放声音
     */
    private void playSound() {
        mp = MediaPlayer.create(this, R.raw.call);
        mp.setLooping(true);
        try {
            mp.prepare();
        } catch (Exception e) {

        }
        mp.start();

    }

    private void stopMusic() {
        try {
            if (mp != null) {
                mp.stop();
                mp.release();
                mp = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 当MediaPlayer.OnErrorListener会运行的Listener */
        if (mp != null) {
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    try {
                        /* 发生错误时也解除资源与MediaPlayer的赋值 */
                        mp.stop();
                        mp.release();
                        mp = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected void onPause() {
        stopMusic();
//		try {
//			if (mPhoneStateReceiver != null) {
//				unregisterReceiver(mPhoneStateReceiver);
//				mPhoneStateReceiver = null;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        super.onPause();
    }
}
