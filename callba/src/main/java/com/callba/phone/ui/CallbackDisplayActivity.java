package com.callba.phone.ui;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.phone.bean.DialAd;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.util.SPUtils;
import com.callba.phone.util.SimpleHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.service.AutoAnswerReceiver;
import com.callba.phone.service.CalllogService;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.TimeFormatUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

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
    private CalllogService calllogService;
    private Button cancel;
    private MediaPlayer mp;
    private UserDao userDao;
    private Gson gson;
    private DialAd dialAd;
    private ImageView background;
    private CircleImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //loadADImage();
        super.onCreate(savedInstanceState);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_status = (TextView) findViewById(R.id.tv_status);
        cancel = (Button) findViewById(R.id.cancel);
        background = (ImageView) findViewById(R.id.iv_call_bg);
        avatar = (CircleImageView) findViewById(R.id.avatar);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("name");
        number = bundle.getString("number");
        if (!bundle.getString("id").equals("")) {
            Bitmap bitmap = ContactsManager.getAvatar(this, bundle.getString("id"), true);
            if (bitmap != null)
                avatar.setImageBitmap(bitmap);
        }
        tv_name.setText(name);
        tv_num.setText(number);
        // tv_status.setText(number);
        calllogService = new CalllogService(this, null);
        callback();
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
        userDao = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                try {
                    List<DialAd> dialAds;
                    dialAds = gson.fromJson(msg, new TypeToken<ArrayList<DialAd>>() {
                    }.getType());
                    if (dialAds.size() > 0) {
                        dialAd = dialAds.get(0);
                        GlobalConfig.getInstance().setDialAd(dialAd);
                        Glide.with(CallbackDisplayActivity.this).load(dialAd.getImage()).into(background);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(String msg) {

            }
        });
        if (GlobalConfig.getInstance().getDialAd() != null)
            Glide.with(CallbackDisplayActivity.this).load(GlobalConfig.getInstance().getDialAd().getImage()).into(background);
        else
            userDao.getAd(4, getUsername(), getPassword());
    }

    /**
     * 统计回拨失败数据
     *
     * @param errorMsg
     * @author zhw
     */
    private void countCallbackFailedData(String errorMsg) {
        String errorTime = TimeFormatUtil.formatTimeRange();
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("errorMsg", errorMsg);
        paramsMap.put("errorTime", errorTime);
        //	MobclickAgent.onEvent(this, "callback_failed", paramsMap);
    }

    private void callback() {

        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == Task.TASK_SUCCESS) {
                    try {
                        String result = (String) msg.obj;
                        String[] content = result.split("\\|");
                        Log.i("dial_result", result);
                        if ("0".equals(content[0])) {
                            //回拨成功，开启自动接听
                            AutoAnswerReceiver.answerPhone(CallbackDisplayActivity.this);
                            calllogService.saveBackCallLog(name, number);
                            if ((boolean)SPUtils.get(CallbackDisplayActivity.this, Constant.PACKAGE_NAME,Constant.KeyboardSetting,true))
                                playSound();
                        } else {
                            //统计回拨失败数据
                            countCallbackFailedData(content[1]);

                            delayFinish();
                        }
                        tv_status.setText(content[1]);
                    } catch (Exception e) {
                        tv_status.setText(R.string.getserverdata_exception);
                        //统计回拨失败数据
                        countCallbackFailedData(getString(R.string.server_error));

                        delayFinish();
                    }
                } else if (msg.what == Task.TASK_NETWORK_ERROR) {
                    tv_status.setText(R.string.network_error);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.network_error));

                    delayFinish();
                } else if (msg.what == Task.TASK_TIMEOUT) {
                    tv_status.setText(R.string.callback_timeout);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.callback_timeout));

                    delayFinish();
                } else if (msg.what == Task.TASK_UNKNOWN_HOST) {
                    tv_status.setText(R.string.conn_failed);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.conn_failed));

                    delayFinish();
                } else {
                    tv_status.setText(R.string.network_error);
                    //统计回拨失败数据
                    countCallbackFailedData(getString(R.string.network_error));

                    delayFinish();
                }
            }

            /**
             * 延迟关闭当前页面
             * @author zhw
             */
            private void delayFinish() {
                //呼叫失败，延迟关闭
                SimpleHandler.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        };
        final Message msg = mHandler.obtainMessage();
        OkHttpUtils.post().url(Interfaces.DIAL_CALLBACK)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .addParams("caller", getUsername())
                .addParams("callee", number)
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
               if (e instanceof UnknownHostException) {
                    msg.what = Task.TASK_UNKNOWN_HOST;
                } else {
                    msg.what = Task.TASK_FAILED;
                }
            }

            @Override
            public void onResponse(String response, int id) {
                msg.what = Task.TASK_SUCCESS;
                msg.obj = response.replace("\n", "").replace("\r", "");
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
