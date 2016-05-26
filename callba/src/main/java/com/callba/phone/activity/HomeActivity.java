package com.callba.phone.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.recharge.RechargeActivity2;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Logger;

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
    @InjectView(R.id.ad)
    WebView webView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    private String yue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        queryUserBalance();
        WebSettings webSettings = webView.getSettings();
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

            /**
             * 用WebView显示图片，可使用这个参数 设置网页布局类型： 1、LayoutAlgorithm.NARROW_COLUMNS ：
             * 适应内容大小 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
             */
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            //WebView加载web资源
            /*if(urls[2]!=null)
            webView.loadUrl(urls[2]);*/
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
        }
    }

    @Override
    public void init() {

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
                showYueDialog();
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
}
