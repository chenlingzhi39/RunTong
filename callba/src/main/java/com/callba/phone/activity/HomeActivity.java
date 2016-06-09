package com.callba.phone.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.activity.recharge.RechargeActivity2;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.logic.login.UserLoginListener;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.BannerLayout;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Created by Administrator on 2016/5/14.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_home,
        toolbarTitle = R.string.home

)
public class HomeActivity extends BaseActivity {
    @InjectView(R.id.recharge)
    TextView recharge;
    @InjectView(R.id.search)
    TextView search;
    @InjectView(R.id.friend)
    TextView friend;
    @InjectView(R.id.mall)
    TextView mall;
    @InjectView(R.id.finance)
    TextView finance;
    @InjectView(R.id.community)
    TextView community;
    @InjectView(R.id.game)
    TextView game;
    @InjectView(R.id.sign_in)
    TextView signIn;
    @InjectView(R.id.banner)
    BannerLayout banner;
    private String yue;
    /* @InjectView(R.id.view_pager)
     AutoScrollViewPager viewPager;
     @InjectView(R.id.indicator)
     CirclePageIndicator indicator;*/
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private SharedPreferenceUtil mPreferenceUtil;
    private ProgressDialog progressDialog;
    private String username;
    private String password;
    private UserDao userDao;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, false, true);
        mPreferenceUtil.commit();
        // 判断是否自动启动
        if (savedInstanceState == null
                && CalldaGlobalConfig.getInstance().isAutoLogin()
                && !LoginController.getInstance().getUserLoginState()) {
            Log.i("MainCallActivity", "auto");
            Logger.i("MainCallActivity", "MainCallActivity  oncreate autoLogin");
            // 登录
            autoLogin();

        } else {


            // 检查内存数据是否正常
            String username = CalldaGlobalConfig.getInstance().getUsername();
            String password = CalldaGlobalConfig.getInstance().getPassword();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                // 重新打开
               gotoWelcomePage();
            }
        }

      /*  WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        String[] urls = CalldaGlobalConfig.getInstance().getAdvertisements();
        if (urls != null) {
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setUseWideViewPort(true);//关键点
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            if (Build.VERSION.SDK_INT >= 11)
                webSettings.setDisplayZoomControls(false);
            webSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
            webSettings.setAllowFileAccess(true); // 允许访问文件
            webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
            webSettings.setSupportZoom(true); // 支持缩放
            webSettings.setLoadWithOverviewMode(true);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int mDensity = metrics.densityDpi;
            Log.d("maomao", "densityDpi = " + mDensity);
            if (mDensity == 240) {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            } else if (mDensity == 160) {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            } else if (mDensity == 120) {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
            } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            } else if (mDensity == DisplayMetrics.DENSITY_TV) {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            } else {
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            }

            *//**
         * 用WebView显示图片，可使用这个参数 设置网页布局类型： 1、LayoutAlgorithm.NARROW_COLUMNS ：
         * 适应内容大小 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
         *//*
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            //WebView加载web资源
            *//*if(urls[2]!=null)
            webView.loadUrl(urls[2]);*//*
            //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                    view.loadUrl(url);
                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    // TODO Auto-generated method stub
                    if (newProgress == 100) {
                        // 网页加载完成
                        progressBar.setVisibility(View.GONE);
                        Log.i("height", webView.getHeight() + "");
                    } else {
                        // 加载中
                        progressBar.setVisibility(View.VISIBLE);
                    }

                }
            });
        }*/
        localImages.add(R.drawable.ad1);
        localImages.add(R.drawable.ad2);
        localImages.add(R.drawable.ad3);
        banner.setViewRes(localImages);
        userDao=new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                String[] result = msg.split("\\|");
                if (result[0].equals("0")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");//获取当前时间
                    String date = formatter.format(new Date(System.currentTimeMillis()));
                    String[] dates = result[1].split(",");
                    if(!date.equals(dates[dates.length-1]))
                    { Intent intent=new Intent(HomeActivity.this,SignInActivity.class);
                    startActivity(intent);
                    }else mPreferenceUtil.putBoolean("is_sign",true,true);
                }else{toast(result[1]);}
            }

            @Override
            public void failure(String msg) {
              toast(msg);
            }
        });
    }

    @Override
    public void init() {

    }
    // 跳转到起始页
    private void gotoWelcomePage() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, WelcomeActivity.class);
        startActivity(intent);

        // 关闭主tab页面
        ActivityUtil.finishMainTabPages();
    }
    @Override
    public void refresh(Object... params) {
        Message msg = (Message) params[0];

        if (msg.what == Task.TASK_GET_USER_BALANCE) {

            if (msg.arg1 == Task.TASK_SUCCESS) {
                String content = (String) msg.obj;
                Logger.i("查询余额返回", content);

                try {
                    String[] result = content.split("\\|");
                    if ("0".equals(result[0])) {
                        // 成功fanhui数据
                        String accountBalance = result[1];
                        CalldaGlobalConfig.getInstance().setAccountBalance(
                                accountBalance);
                        Log.i("yue", accountBalance);
                        yue = result[1];
                        showYueDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    @OnClick({R.id.recharge, R.id.search, R.id.friend, R.id.mall, R.id.finance, R.id.community, R.id.game, R.id.sign_in})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recharge:
                Intent intent = new Intent(HomeActivity.this, RechargeActivity2.class);
                startActivity(intent);
                break;
            case R.id.search:
                Log.i("home", "search_yue");
                queryUserBalance();

                break;
            case R.id.friend:
                Intent intent1 = new Intent(HomeActivity.this, FriendActivity.class);
                startActivity(intent1);
                break;
            case R.id.mall:
                break;
            case R.id.finance:
                break;
            case R.id.community:
                Intent intent0 = new Intent(HomeActivity.this, CommunityActivity.class);
                startActivity(intent0);
                break;
            case R.id.game:
                break;
            case R.id.sign_in:
                Intent intent2 = new Intent(HomeActivity.this, SignInActivity.class);
                startActivity(intent2);
                break;
        }
    }


    public class YueDialogHelper implements DialogInterface.OnDismissListener {
        @InjectView(R.id.recharge)
        Button recharge;
        @InjectView(R.id.exchange)
        Button exchange;
        @InjectView(R.id.number)
        TextView number;
        @InjectView(R.id.yue)
        TextView tv_yue;
        @InjectView(R.id.gold)
        TextView gold;
        @InjectView(R.id.time)
        TextView time;
        private View mView;
        private Dialog mDialog;

        public YueDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_yue, null);
            ButterKnife.inject(this, mView);
            number.setText(number.getText().toString() + "   " + CalldaGlobalConfig.getInstance().getUsername());
            if (!yue.equals(""))
                tv_yue.setText(tv_yue.getText().toString() + "   " + yue);
        }

        @OnClick(R.id.recharge)
        public void recharge() {
            Intent intent = new Intent(HomeActivity.this, RechargeActivity2.class);
            startActivity(intent);
        }

        @OnClick(R.id.exchange)
        public void exchange() {

        }

        public View getView() {
            return mView;
        }

        public void setDialog(Dialog mDialog) {
            this.mDialog = mDialog;
        }

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }
    }

    private void showYueDialog() {
        YueDialogHelper helper = new YueDialogHelper();
        Dialog dialog = new AlertDialog.Builder(this, R.style.MyDialogStyle)
                .setView(helper.getView())
                .setOnDismissListener(helper)
                .create();
        helper.setDialog(dialog);
        dialog.show();
    }

    /**
     * 查询用户余额
     */
    private void queryUserBalance() {
        Task task = new Task(Task.TASK_GET_USER_BALANCE);
        Map<String, Object> taskParams = new HashMap<String, Object>();
        Log.i("name", CalldaGlobalConfig.getInstance()
                .getUsername());
        Log.i("pwd", CalldaGlobalConfig.getInstance()
                .getPassword());
        taskParams.put("loginName", CalldaGlobalConfig.getInstance()
                .getUsername());
        taskParams.put("loginPwd", CalldaGlobalConfig.getInstance()
                .getPassword());
        taskParams.put("softType", "android");
        taskParams.put("frompage", "MainCallActivity");
        task.setTaskParams(taskParams);

        MainService.newTask(task);
    }
    /**
     * 自动登录
     */
    private void autoLogin() {
        username = mPreferenceUtil.getString(Constant.LOGIN_USERNAME);
        password = mPreferenceUtil.getString(Constant.LOGIN_PASSWORD);

        if ("".equals(CalldaGlobalConfig.getInstance().getSecretKey())) {
             Log.i("home","nosecret");
            // 跳转到起始页
            gotoWelcomePage();
            return;

        } else if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

            // 保存的数据被清空，跳转到手动登录界面
            switchManualLogin();
            return;
        }

        // 加密，生成loginSign
        String source = username + "," + password;
        String sign = null;
        try {
            sign = DesUtil.encrypt(source, CalldaGlobalConfig.getInstance()
                    .getSecretKey());
        } catch (Exception e) {
            e.printStackTrace();

            // 手动登录
            switchManualLogin();
            return;
        }

        progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.logining));


        Task task = new Task(Task.TASK_LOGIN);
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("loginSign", sign);
        taskParams.put("loginType", "0");
        task.setTaskParams(taskParams);

        // 登录
        LoginController.getInstance().userLogin(this, task,
                new UserLoginListener() {
                    @Override
                    public void serverLoginFailed(String info) {
                        if (progressDialog != null
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        toast(info);
                        // 手动登录
                        switchManualLogin();
                    }

                    @Override
                    public void loginSuccess(String[] resultInfo) {
                        if (progressDialog != null
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();

                        }


                        // 处理登录成功返回信息
                        LoginController.parseLoginSuccessResult(
                                HomeActivity.this, username, password,
                                resultInfo);
                        if(!mPreferenceUtil.getBoolean("is_sign",false)) {
                            String year = Calendar.getInstance().get(Calendar.YEAR) + "";
                            String month = Calendar.getInstance().get(Calendar.MONTH) + 1 + "";
                            if (month.length() == 1)
                                month = "0" + month;
                            userDao.getMarks(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(), year + month);
                        }


                        // 查询余额
                        //queryUserBalance();
                    }

                    @Override
                    public void localLoginFailed(UserLoginErrorMsg errorMsg) {
                        if (progressDialog != null
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        // 解析登录失败信息
                        LoginController.parseLocalLoginFaildInfo(
                                getApplicationContext(), errorMsg);
                        // 手动登录
                        switchManualLogin();
                    }
                });

    }

    /**
     * 跳转到手动登陆界面
     */
    private void switchManualLogin() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, LoginActivity.class);
        startActivity(intent);

        // 关闭主tab页面
        ActivityUtil.finishMainTabPages();
    }


}
