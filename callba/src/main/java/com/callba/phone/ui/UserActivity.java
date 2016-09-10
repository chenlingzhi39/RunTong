package com.callba.phone.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.BuildConfig;
import com.callba.R;
import com.callba.phone.service.UpdateService;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.SimpleHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_user,
        toolbarTitle = R.string.center,
        menuId = R.menu.menu_user
)
public class UserActivity extends BaseActivity {
    @InjectView(R.id.user_head)
    CircleImageView userHead;
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.word)
    TextView word;
    @InjectView(R.id.account)
    RelativeLayout account;
    @InjectView(R.id.change_info)
    RelativeLayout changeInfo;
    @InjectView(R.id.change_password)
    RelativeLayout changePassword;
    @InjectView(R.id.retrieve)
    RelativeLayout retrieve;
    @InjectView(R.id.about)
    RelativeLayout about;
    @InjectView(R.id.help)
    RelativeLayout help;
    @InjectView(R.id.update)
    RelativeLayout update;
    @InjectView(R.id.logout)
    RelativeLayout logout;
    @InjectView(R.id.version_code)
    TextView versionCode;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        if(UserManager.getNickname(this).equals(""))
        number.setText(getUsername());
        else number.setText(UserManager.getNickname(this));
        versionCode.setHint(BuildConfig.VERSION_NAME);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("正在检查更新");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setCancelable(true);
    }

    @Override
    protected void onResume() {
        if(UserManager.getNickname(this).equals(""))
            number.setText(getUsername());
        else number.setText(UserManager.getNickname(this));
                if (!UserManager.getUserAvatar(UserActivity.this).equals(""))
                    Glide.with(UserActivity.this).load(UserManager.getUserAvatar(UserActivity.this)).into(userHead);
                if (!UserManager.getSignature(UserActivity.this).equals("")) {
                    word.setText(UserManager.getSignature(UserActivity.this));
                }
        super.onResume();
    }


    @OnClick({R.id.account, R.id.change_info, R.id.change_password, R.id.retrieve, R.id.logout, R.id.about, R.id.help, R.id.update, R.id.user_head})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.account:
                intent = new Intent(UserActivity.this, AccountActivity.class);
                startActivity(intent);
                break;
            case R.id.change_info:
                intent = new Intent(UserActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.change_password:
                intent = new Intent(UserActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.retrieve:
                intent = new Intent(UserActivity.this, RetrievePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("确认退出登录？");
                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                UserManager.putPassword(UserActivity.this,"");
                                UserManager.putOriginalPassword(UserActivity.this,"");
                                LoginController.getInstance().setUserLoginState(false);
                                Intent intent0 = new Intent("com.callba.location");
                                intent0.putExtra("action", "logout");
                                sendBroadcast(intent0);
                                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                                MobclickAgent.onProfileSignOff();
                                finish();

                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                dialog.dismiss();


                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setCancelable(true);
                alertDialog.show();

                break;
            case R.id.about:
                intent = new Intent(UserActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.help:
                intent = new Intent(UserActivity.this, HelpActivity.class);
                startActivity(intent);
                break;
            case R.id.update:
                Log.i("click", "update");
                if (!UpdateService.is_downloading)
                {progressDialog.show();
                sendGetVersionTask();}else{toast("正在下载更新");}
                break;
            case R.id.user_head:
                intent = new Intent(UserActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 发送获取版本信息任务
     *
     * @author zhw
     */
    private void sendGetVersionTask() {
            OkHttpUtils.post().url(Interfaces.Version)
                .addParams("softType","android")
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
               progressDialog.show();
            }

            @Override
            public void onAfter(int id) {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                if(e instanceof UnknownHostException){
                    toast(R.string.conn_failed);
                }else{toast(R.string.network_error);}
            }

            @Override
            public void onResponse(String response, int id) {
                try{
                String[] result=response.split("\\|");
                if(result[0].equals("0"))
                {AppVersionChecker.AppVersionBean appVersionBean=AppVersionChecker.parseVersionInfo(UserActivity.this,response);
                GlobalConfig.getInstance().setAppVersionBean(appVersionBean);
                checkLoginKey(appVersionBean);}
                else toast(result[1]);
                }catch(Exception e){
                    toast(R.string.getserverdata_exception);
                }
            }
        });
    }

    /**
     * 检查是否成功获取加密的key
     *
     * @author zhw
     */
    private void checkLoginKey(AppVersionChecker.AppVersionBean appVersionBean) {
        // Logger.i(TAG, "getSecretKey() : " +
        // GlobalConfig.getInstance().getSecretKey());
        //Logger.i(TAG, "currentGetVersionTime : " + currentGetVersionTime);

        if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {
            // 成功获取key
            check2Upgrade(appVersionBean, true);
            UserManager.putSecretKey(UserActivity.this,appVersionBean.getSecretKey());
        } else {
            // 统计获取版本失败次数
            //MobclickAgent.onEvent(this, "version_timeout");
            String secretKey = UserManager.getSecretKey(this);
            if (TextUtils.isEmpty(secretKey)) {
                // Toast.makeText(this, R.string.getversionfailed,
                // Toast.LENGTH_SHORT).show();
                // 提示用户获取失败
                //alertUserGetVersionFailed();
                toast(R.string.net_error_getdata_fail);
            } else {
                check2Upgrade(appVersionBean, true);
            }
        }
    }


    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
               /* Intent intent = new Intent(MessageActivity.this, PostMessageActivity.class);
                startActivity(intent);*/
                Intent intent = new Intent(UserActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.opinion:
                startActivity(new Intent(UserActivity.this,OpinionActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
